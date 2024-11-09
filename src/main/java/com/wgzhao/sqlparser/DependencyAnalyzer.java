package com.wgzhao.sqlparser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class DependencyAnalyzer
{
    public static Map<String, Set<String>> analyzeDependencies(Map<String, List<SqlElement>> sqlMap) {
        // Step 1: Build a reverse map from targetTable to SQL IDs
        Map<String, Set<String>> targetToSqlIds = new HashMap<>();
        for (Map.Entry<String, List<SqlElement>> entry : sqlMap.entrySet()) {
            String sqlId = entry.getKey();
            for (SqlElement element : entry.getValue()) {
                targetToSqlIds.computeIfAbsent(element.getTarget(), k -> new HashSet<>()).add(sqlId);
            }
        }

        // Step 2: Build the dependency map
        Map<String, Set<String>> dependencies = new HashMap<>();
        for (String sqlId : sqlMap.keySet()) {
            dependencies.put(sqlId, new HashSet<>());
            for (SqlElement element : sqlMap.get(sqlId)) {
                for (String sourceTable : element.getSource()) {
                    if (targetToSqlIds.containsKey(sourceTable)) {
                        dependencies.get(sqlId).addAll(targetToSqlIds.get(sourceTable));
                    }
                }
            }
        }

        // Step 3: Remove transitive dependencies
        for (String sqlId : dependencies.keySet()) {
            Set<String> directDependencies = dependencies.get(sqlId);
            Set<String> allDependencies = new HashSet<>(directDependencies);
            Stack<String> stack = new Stack<>();
            stack.addAll(directDependencies);
            while (!stack.isEmpty()) {
                String current = stack.pop();
                if (dependencies.containsKey(current))
                    for (String dep : dependencies.get(current)) {
                        if (allDependencies.add(dep))
                            stack.push(dep);
                    }
            }
            allDependencies.removeAll(directDependencies);
            ((Set)dependencies.get(sqlId)).removeAll(allDependencies);
        }

        return dependencies;
    }

    private static Set<String> getAllDependencies(String sqlId, Map<String, Set<String>> dependencies) {
        Set<String> allDependencies = new HashSet<>();
        if (dependencies.containsKey(sqlId)) {
            for (String dep : dependencies.get(sqlId)) {
                allDependencies.add(dep);
                allDependencies.addAll(getAllDependencies(dep, dependencies));
            }
        }
        return allDependencies;
    }
}
