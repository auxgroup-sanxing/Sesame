package com.sanxing.studio.search;

public class ProjectRecord
    extends Record
    implements SearcherName
{
    @Override
    public Column getDescriptionColumn()
    {
        return getColumnByName( "project description" );
    }

    @Override
    public Column getNameColumn()
    {
        return getColumnByName( "project name" );
    }

    @Override
    public String getSearcherName()
    {
        return "project";
    }
}