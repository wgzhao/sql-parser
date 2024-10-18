package com.wgzhao.sqlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SqlElement
{
    public SqlType type;
    public String target;
    public Set<String> columns;
    public Set<String> source;

    public SqlElement(SqlType type, String target, Set<String> columns, Set<String> source)
    {
        this.type = type;
        this.target = target;
        this.columns = columns;
        this.source = source;
    }
    public SqlElement()
    {
        this.type = null;
        this.target = null;
        this.columns = null;
        this.source = null;
    }

    public SqlType getType()
    {
        return type;
    }

    public void setType(SqlType type)
    {
        this.type = type;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public Set<String> getColumns()
    {
        return columns;
    }

    public void setColumns(Set<String> columns)
    {
        this.columns = columns;
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
                "type=" + type +
                ", target='" + target + '\'' +
                ", columns=" + columns +
                ", source=" + source +
                '}';
    }

    public void pretty() {
        System.out.printf("""
                type: \t%s
                target: \t%s
                columns: \t%s
                source: \t%s
                ---------
                """, type, target, columns, source);
    }
}
