package com.sanxing.sesame.jdbc.template.type;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DateTypeHandler
    implements TypeHandler
{
    @Override
    public Object getField( ResultSet rs, int index )
        throws SQLException
    {
        Date value = rs.getDate( index );
        if ( rs.wasNull() )
        {
            value = null;
        }
        return value;
    }

    @Override
    public void setParameter( PreparedStatement ps, int index, Object value )
        throws SQLException
    {
        Date v = null;
        if (value instanceof Date)
        {
            v = (Date) value;
        }
        else
        {
            v = new Date( ((java.util.Date) value).getTime() );
        }
        ps.setDate( index, v );
    }
}