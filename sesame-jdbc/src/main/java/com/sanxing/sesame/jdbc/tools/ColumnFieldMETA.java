package com.sanxing.sesame.jdbc.tools;

public class ColumnFieldMETA
{
    private String fieldName;

    private String columnName;

    private String columnType;

    private int length;

    private String desc;

    private boolean pk;

    public boolean isPk()
    {
        return pk;
    }

    public void setPk( boolean pk )
    {
        this.pk = pk;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName( String fieldName )
    {
        this.fieldName = fieldName;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName( String columnName )
    {
        this.columnName = columnName;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength( int length )
    {
        this.length = length;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc( String desc )
    {
        this.desc = desc;
    }

    public String getColumnType()
    {
        return columnType;
    }

    public void setColumnType( String columnType )
    {
        this.columnType = columnType;
    }

    @Override
    public String toString()
    {
        return "FieldMETA [columnName=" + columnName + ", columnType=" + columnType + ", desc=" + desc + ", fieldName="
            + fieldName + ", length=" + length + "]";
    }
}