package com.sanxing.studio.search;

public class Column
{
    private String name;

    private String value;

    private boolean indexAnalyzed;

    public Column( String name, String value, boolean analyzed )
    {
        this.name = name;
        this.value = value;
        indexAnalyzed = analyzed;
    }

    public Column( Column col )
    {
        name = col.name;
        value = col.value;
        indexAnalyzed = col.indexAnalyzed;
    }

    public boolean isIndexAnalyzed()
    {
        return indexAnalyzed;
    }

    public void setIndexAnalyzed( boolean indexAnalyzed )
    {
        this.indexAnalyzed = indexAnalyzed;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }
}