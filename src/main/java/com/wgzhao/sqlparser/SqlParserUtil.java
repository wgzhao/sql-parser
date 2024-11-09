package com.wgzhao.sqlparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParserUtil
{
    /**
     * Parse giving SQL String , extract the target table and source tables
     * using regex pattern
     * @param sql SQL string
     * @return List of {@link SqlElement}
     */
    public static List<SqlElement> getTables(String sql) {
        // Preprocess the SQL string
        sql = preprocess(sql);
        // Parse the SQL string and return a list of SqlElement objects
        List<SqlElement> result = new ArrayList<>();
        String[] split = sql.split(";");
        List<String> targetTables = new ArrayList<>();
        for (String s : split) {
            // Regex pattern to match the target table in an INSERT statement
            Pattern targetPattern = Pattern.compile("(?:INSERT\\s+INTO|INSERT\\s+OVERWRITE\\s+TABLE|INSERT\\s+OVERWRITE)\\s+([^\\s(]+)", Pattern.CASE_INSENSITIVE);
            // Find the target table
            Matcher targetMatcher = targetPattern.matcher(s);
            String targetTable = targetMatcher.find() ? targetMatcher.group(1) : null;
            if (targetTable == null) {
                System.out.println("No target table found, skipping");
                continue;
            }

            // Regex pattern to match the source tables in a SELECT statement, including subqueries and CTEs
            Pattern sourcePattern = Pattern.compile("(?:FROM|JOIN)\\s+([^\\s,();]+)", Pattern.CASE_INSENSITIVE);

            // Regex pattern to match CTEs
            Pattern ctePattern = Pattern.compile("with\\s+([\\w\\s]+)\\s+as\\s+\\((.*\\))", Pattern.CASE_INSENSITIVE);

            Pattern cteSubsequentPattern = Pattern.compile(",\\s+([\\w\\s,]+)\\s+as\\s+\\((.*\\))", Pattern.CASE_INSENSITIVE);

            // Find all CTEs and extract their source tables
            Matcher cteMatcher = ctePattern.matcher(s);
            Set<String> cteTables = new HashSet<>();
            Set<String> cteAliasTables = new HashSet<>();
            while (cteMatcher.find()) {
                cteAliasTables.add(cteMatcher.group(1));
                String cteSql = cteMatcher.group(2);
                Matcher cteSourceMatcher = sourcePattern.matcher(cteSql);
                while (cteSourceMatcher.find()) {
                    cteTables.add(cteSourceMatcher.group(1));
                }
            }
            Matcher matcher = cteSubsequentPattern.matcher(s);
            while (matcher.find()) {
                cteAliasTables.add(matcher.group(1));
                String cteSql = matcher.group(2);
                matcher = cteSubsequentPattern.matcher(cteSql);
            }

            // Remove CTEs from the main SQL to avoid double counting
            String sqlWithoutCtes = sql.replaceAll(ctePattern.pattern(), "");

            // Find all source tables in the main SQL
            Matcher sourceMatcher = sourcePattern.matcher(sqlWithoutCtes);
            Set<String> sourceTables = new HashSet<>();
            while (sourceMatcher.find()) {
                sourceTables.add(sourceMatcher.group(1));
            }

            // Combine source tables from main SQL and CTEs
            sourceTables.addAll(cteTables);
            sourceTables.removeAll(cteAliasTables);
            if (targetTables.contains(targetTable)) {
                System.out.println("Duplicate target table found, merge source tables");
                int index = targetTables.indexOf(targetTable);
                Set<String> sources = result.get(index).getSource();
                sources.addAll(sourceTables);
                result.get(index).setSource(sources);
            } else {
                targetTables.add(targetTable);
                result.add(new SqlElement(targetTable, sourceTables));
            }

        }
        return result;
    }

//    public static List<SqlElement> getStatementType(String sql)
//            throws JSQLParserException, ParseException
//    {
//        List<SqlElement> allResult = new ArrayList<>();
//
//        CCJSqlParser parser = new CCJSqlParser(sql);
//        Statements sts = parser.Statements();
//        for (Statement statement : sts) {
//            // Check the type of the statement
//            SqlElement result = new SqlElement();
//            Select select = null;
//            if (!(statement instanceof Insert)) {
//                System.out.println("Not an insert statement, skipping");
//                continue;
//            }
//
//            Insert insert = (Insert) statement;
//            result.setTarget(insert.getTable().getName());
//            List<Column> insertColumns = insert.getColumns();
//
//            select = insert.getSelect();
//
//            if (select != null) {
//
//                Map<String, String> aliasToTableMap = getAliasToTableMap(select);
//                List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
//                assert  selectItems.size() == insertColumns.size();
////                Map<String, Set<String>> columnsMap = new HashMap<>();
////                for(int i =0; i< insertColumns.size(); i++) {
////                    Set<String> sourceColumns = new HashSet<>();
////                    resolveColumnNames(selectItems.get(i).getExpression(), aliasToTableMap, sourceColumns);
////                    columnsMap.put(insertColumns.get(i).getColumnName(), sourceColumns);
////                }
////                result.setColumnsMap(columnsMap);
//                result.setSource(TablesNamesFinder.findTables(select.toString()));
//            }
//            allResult.add(result);
//        }
//
//        return allResult;
//    }

//    private static Map<String, String> getAliasToTableMap(Select selectStatement)
//    {
//        Map<String, String> aliasToTableMap = new HashMap<>();
//        FromItem fromItem = selectStatement.getPlainSelect().getFromItem();
//        if (fromItem instanceof Table) {
//            Table table = (Table) fromItem;
//            String tableName = table.getName();
//            String alias = table.getAlias() == null ? tableName : table.getAlias().getName();
//            aliasToTableMap.put(alias, tableName);
//        }
//
//        // Handle join
//        List<Join> joins = selectStatement.getPlainSelect().getJoins();
//        if (joins != null) {
//            for (Join join : joins) {
//                FromItem joinItem = join.getRightItem();
//                if (joinItem instanceof Table) {
//                    Table table = (Table) joinItem;
//                    String tableName = table.getName();
//                    String alias = table.getAlias() != null ? table.getAlias().getName() : tableName;
//                    aliasToTableMap.put(alias, tableName);
//                }
//            }
//        }
//        return aliasToTableMap;
//
////        try {
////            Set<String> tables = TablesNamesFinder.findTables(selectStatement.toString());
////            for (String table : tables) {
////                String[] parts = table.split(" ");
////                if (parts.length == 2) {
////                    aliasToTableMap.put(parts[1], parts[0]);
////                } else {
////                    aliasToTableMap.put(parts[0], parts[0]);
////                }
////            }
////            return aliasToTableMap;
////        }
////        catch (JSQLParserException e) {
////            return null;
////        }
//    }

//    private static void resolveColumnNames(Expression expression, Map<String, String> aliasToTableMap, Set<String> columns)
//    {
//        if (expression instanceof Column) {
//            Column column = (Column) expression;
//            String tableAlias = column.getTable().getName();
//            String actualTableName = aliasToTableMap.get(tableAlias);
//            if (actualTableName != null) {
//                columns.add(actualTableName + "." + column.getColumnName());
//            }
//            else {
//                columns.add(column.getColumnName());
//            }
//        }
//        else if (expression instanceof Function) {
//            Function function = (Function) expression;
//            List<Expression> expressions = (List<Expression>) function.getParameters().getExpressions();
//            for (Expression param : expressions) {
//                resolveColumnNames(param, aliasToTableMap, columns);
//            }
//        }
//    }

    private static String preprocess(String sql)
    {

        // Remove the OVERWRITE keyword
//        String regexOverwrite = "\\bINSERT\\s+(OVERWRITE|INTO)\\s+TABLE\\b";
//        Pattern patternOverwrite = Pattern.compile(regexOverwrite, Pattern.CASE_INSENSITIVE);
//        Matcher matcherOverwrite = patternOverwrite.matcher(sql);
//        sql = matcherOverwrite.replaceAll("insert into");
//
//        // Remove the PARTITION(dt) clause
//        String regexPartition = "\\bpartition\\s*\\([^)]*\\)";
//        Pattern patternPartition = Pattern.compile(regexPartition, Pattern.CASE_INSENSITIVE);
//        Matcher matcherPartition = patternPartition.matcher(sql);
//        sql = matcherPartition.replaceAll("");

        return sql
                // Remove single-line comments starting with --
                .replaceAll("--.*?(\r?\n|$)", "")

                // Remove single-line comments starting with #
                .replaceAll("#.*?(\r?\n|$)", "")

                // Remove multi-line comments enclosed in /* */
                .replaceAll("/\\*.*?\\*/", "")

                // Remove unnecessary white spaces (multiple spaces, tabs, newlines)
                .replaceAll("\\s+", " ")
                // use constant _var to replace  like ${var} from the sql
                .replaceAll("\\$\\{[^}]+\\}", "_var")
                .trim();
    }
}
