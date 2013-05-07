package com.sanxing.sesame.jdbc.template.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import com.sanxing.sesame.collection.IgnoreCaseMap;
import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.RowMapping;

public class MapRowMapping
    implements RowMapping
{
    private static MapRowMapping mapRowMapping = new MapRowMapping();

    public static MapRowMapping getInstance()
    {
        return mapRowMapping;
    }

    @Override
    public Object handleRow( ResultSet rs )
        throws SQLException
    {
        Map row = new IgnoreCaseMap();
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int count = rsMetaData.getColumnCount();
        if ( count > 0 )
        {
            for ( int i = 1; i <= count; ++i )
            {
                String colName = rsMetaData.getColumnName( i );
                try
                {
                    Object value = rs.getObject( i );
                    row.put( colName, value );
                }
                catch ( Exception e )
                {
                    throw new DataAccessException( "column[" + colName + "] error:" + e.getMessage(), e );
                }
            }
        }
        return row;
    }
}