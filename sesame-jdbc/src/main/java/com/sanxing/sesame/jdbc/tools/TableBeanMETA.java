package com.sanxing.sesame.jdbc.tools;

import java.util.LinkedList;
import java.util.List;

public class TableBeanMETA
{
    private String clazzName;

    private String tableName;

    private final List<ColumnFieldMETA> fields = new LinkedList();

    public String getClazzName()
    {
        return clazzName;
    }

    public void setClazzName( String clazzName )
    {
        this.clazzName = clazzName;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName( String tableName )
    {
        this.tableName = tableName;
    }

    public void addField( ColumnFieldMETA field )
    {
        fields.add( field );
    }

    public List<ColumnFieldMETA> getFields()
    {
        return fields;
    }

    @Override
    public String toString()
    {
        String temp = "TableBeanMETA [clazzName=" + clazzName + " tableName=" + tableName + "]\n";
        temp = temp + "===================fields===============\n";
        for ( ColumnFieldMETA field : fields )
        {
            temp = temp + field.toString() + "\n";
        }
        temp = temp + "===================fields===============\n";
        return temp;
    }
}