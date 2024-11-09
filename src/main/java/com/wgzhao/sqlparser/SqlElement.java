package com.wgzhao.sqlparser;

import java.util.Map;
import java.util.Set;

public class SqlElement
{
    public String target;
    // {"col1": ["tbl1.col1", "tbl2.col2"], "col2": ["tbl1.col2", "tbl2.col3"]}
    public Map<String, Set<String>> columnsMap;
    public Set<String> source;

    public SqlElement(String target, Map<String, Set<String>> columnsMap, Set<String> source)
    {
        this.target = target;
        this.columnsMap = columnsMap;
        this.source = source;
    }

    public SqlElement(String target ,Set<String> source) {
        this.target = target;
        this.columnsMap = null;
        this.source = source;
    }

    public SqlElement()
    {
        this.target = null;
        this.columnsMap = null;
        this.source = null;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public Map<String, Set<String>> getColumnsMap()
    {
        return columnsMap;
    }

    public void setColumnsMap(Map<String, Set<String>> columnsMap)
    {
        this.columnsMap = columnsMap;
    }

    public Set<String> getSource()
    {
        return source;
    }

    public void setSource(Set<String> source)
    {
        this.source = source;
    }

    public String toString()
    {
        return "SqlElement{" +
                ", target='" + target + '\'' +
                ", columns=" + columnsMap +
                ", source=" + source +
                '}';
    }

    public void pretty() {
        System.out.printf("""
                target: \t%s
                columns: \t%s
                source: \t%s
                ---------
                """, target, columnsMap, source);
    }
}
