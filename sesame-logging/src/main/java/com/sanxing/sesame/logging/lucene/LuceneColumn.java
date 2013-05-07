package com.sanxing.sesame.logging.lucene;

public class LuceneColumn
{
    private String name;

    private String value;

    private boolean indexAnalyzed;

    public LuceneColumn( String name, String value, boolean analyzed )
    {
        this.name = name;
        this.value = value;
        indexAnalyzed = analyzed;
    }

    public LuceneColumn( LuceneColumn col )
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