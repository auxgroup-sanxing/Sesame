package com.sanxing.studio.search;

public class ElementRecord
    extends Record
    implements SearcherName
{
    private String name;

    private String description;

    public ElementRecord( String name, String description )
    {
        this.name = name;
        this.description = description;
        addField( new Column( "element name", name, true ) );
        addField( new Column( "element description", description, true ) );
    }

    public ElementRecord()
    {
    }

    @Override
    public String getSearcherName()
    {
        return "element";
    }

    @Override
    public Column getDescriptionColumn()
    {
        return getColumnByName( "element description" );
    }

    @Override
    public Column getNameColumn()
    {
        return getColumnByName( "element name" );
    }
}