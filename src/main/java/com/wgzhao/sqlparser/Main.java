package com.wgzhao.sqlparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main
{
//    public static Callable<Map<String, Object>> extractSql(String sql)
//    {
//        return () -> {
//            return getStatementType(sql);
//        };
//    }

    public static List<SqlElement> getStatementType(String sql)
            throws JSQLParserException
    {
        List<SqlElement> allResult = new ArrayList<>();
        for (String ss : sql.split(";")) {
            // Parse the SQL statement
            Statement statement = CCJSqlParserUtil.parse(ss);
            // Check the type of the statement
            SqlElement result = new SqlElement();
            Select select = null;
            switch (statement) {
                case Select select1 -> {
                    result.setType(SqlType.Select);
                    select = select1;
                }
                case Update update -> {
                    result.setType(SqlType.Update);
                    result.setTarget(update.getTable().getName());
                    final Set<String> temp = new HashSet<>();
                    for (UpdateSet us : update.getUpdateSets()) {
                        us.getColumns().forEach(column -> temp.add(column.toString()));
                        for (Object o : us.getValues()) {
                            if (o instanceof Select s) {
                                result.setSource(TablesNamesFinder.findTables(s.toString()));
                            }
                        }
                    }
                    result.setColumns(temp);
                }
                case Insert insert -> {
                    result.setType(SqlType.Insert);
                    result.setTarget(insert.getTable().getName());
                    select = insert.getSelect();
                }
                case Delete delete -> {
                    result.setType(SqlType.Delete);
                    result.setTarget(delete.getTable().getName());
                }
                case null, default -> {
                    result.setType(SqlType.Unknow);
                }
            }
            if (select != null) {
                Set<String> temp = new HashSet<>();
                try {
                    select.getPlainSelect().getSelectItems().forEach(selectItem -> {
                        if (selectItem.getAlias() == null) {
                            temp.add(selectItem.toString());
                        }
                        else {
                            temp.add(selectItem.getAlias().getName());
                        }
                    });
                } catch (ClassCastException _ignored){
                    ((SetOperationList) select).getSelects().forEach(selectItem -> {
                        selectItem.getPlainSelect().getSelectItems().forEach(selectItem1 -> {
                            if (selectItem1.getAlias() == null) {
                                temp.add(selectItem1.toString());
                            }
                            else {
                                temp.add(selectItem1.getAlias().getName());
                            }
                        });
                    });
                }
                result.setColumns(temp);
                result.setSource(TablesNamesFinder.findTables(select.toString()));
            }
            allResult.add(result);
        }

        return allResult;
    }

    public static String preprocess(String sql)
    {

        return sql
                // Remove single-line comments starting with --
                .replaceAll("--.*?(\r?\n|$)", "")

                // Remove single-line comments starting with #
                .replaceAll("#.*?(\r?\n|$)", "")

                // Remove multi-line comments enclosed in /* */
                .replaceAll("/\\*.*?\\*/", "")

                // Remove unnecessary white spaces (multiple spaces, tabs, newlines)
                .replaceAll("\\s+", " ").trim();
    }

    public static List<Path> getAllSqlFiles(String directoryPath)
    {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath), 1, FileVisitOption.FOLLOW_LINKS)) {
            return paths
                    .filter(Files::isRegularFile) // Filter to include only regular files
                    .filter(path -> path.toString().endsWith(".sql")) // Filter to include only .sql files
                    .collect(Collectors.toList()); // Collect the results into a list
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void main(String[] args)
    {
        String dir = ".";
        if (args.length == 1) {
            dir = args[0];
        }
        List<Path> sqlFileList = getAllSqlFiles(dir);
        sqlFileList.forEach(path -> {
            System.out.printf("File: %s%n", path);
            try {
                String sql = preprocess(Files.readString(path));
                List<SqlElement> result = getStatementType(sql);
                result.forEach(SqlElement::pretty);
                System.out.println();
            }
            catch (IOException | JSQLParserException e) {
                e.printStackTrace();
            }
        });
    }
}
