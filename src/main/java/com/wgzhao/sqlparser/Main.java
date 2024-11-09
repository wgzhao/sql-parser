package com.wgzhao.sqlparser;

import net.sf.jsqlparser.expression.JsonAggregateUniqueKeysType;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main
{

    public static List<Path> getAllSqlFiles(String directoryPath)
    {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath), 2, FileVisitOption.FOLLOW_LINKS)) {
            return paths
                    .filter(Files::isRegularFile) // Filter to include only regular files
                    .filter(path -> path.toString().endsWith(".sql")) // Filter to include only .sql files
                    .collect(Collectors.toList()); // Collect the results into a list
        }
        catch (IOException e) {
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
        Map<String, List<SqlElement>> deps = new HashMap<>();
        List<Path> sqlFileList = getAllSqlFiles(dir);
        sqlFileList.forEach(path -> {
            System.out.printf("File: %s%n", path);
            String id = path.getFileName().toString().split("-")[0];
            String sql = null;
            try {
                sql = Files.readString(path);
                List<SqlElement> result = SqlParserUtil.getTables(sql);
                deps.put(id, result);
            }
            catch (Exception  e) {
                System.out.println(sql);
                e.printStackTrace();
            }
        });

        System.out.println("ID");
        deps.forEach((k, v) -> {
            System.out.println(k);
            v.forEach(e -> {
                System.out.print("\t" + e.getTarget() + " <-- ");
                System.out.println(String.join(",", e.getSource()));
            });

        });

        System.out.println(DependencyAnalyzer.analyzeDependencies(deps));
    }
}
