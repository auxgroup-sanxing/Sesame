package com.sanxing.studio.search;

import java.util.ArrayList;
import java.util.List;

public abstract class Record
    implements SearcherName, Comparable<Record>
{
    public List<Column> columns;

    private String searcherName;

    public Record()
    {
        columns = new ArrayList();
    }

    public void addField( Column column )
    {
        columns.add( column );
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public Column getColumnByName( String name )
    {
        for ( Column e : getColumns() )
        {
            if ( e.getName().equalsIgnoreCase( name ) )
            {
                return e;
            }
        }
        return null;
    }

    public abstract Column getNameColumn();

    public abstract Column getDescriptionColumn();

    public void setIndexAnalyzed( boolean indexAnalyzed )
    {
        for ( Column column : columns )
        {
            column.setIndexAnalyzed( indexAnalyzed );
        }
    }

    public void join( Record base, boolean indexAnalyzed )
    {
        List<Column> baseColumns = base.getColumns();
        for ( Column e : baseColumns )
        {
            Column ne = new Column( e );
            ne.setIndexAnalyzed( indexAnalyzed );
            columns.add( ne );
        }
    }

    public void setSearcherName( String searcherName )
    {
        this.searcherName = searcherName;
    }

    public String getType()
    {
        return getSearcherName();
    }

    @Override
    public int compareTo( Record o )
    {
        return -1;
    }

    @Override
    public String toString()
    {
        String name = "{";
        for ( Column column : columns )
        {
            name = name + column.getName() + "=" + column.getValue() + " isIndexAnalyzed=" + column.isIndexAnalyzed();
            name = name + "\t";
        }
        name = name + "}";
        return name;
    }
}