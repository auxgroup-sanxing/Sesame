package com.sanxing.studio.search;

public class DummyRecord
    extends Record
    implements SearcherName
{
    public String getColumnName()
    {
        return null;
    }

    @Override
    public String getSearcherName()
    {
        return null;
    }

    @Override
    public Column getDescriptionColumn()
    {
        return null;
    }

    @Override
    public Column getNameColumn()
    {
        return null;
    }
}