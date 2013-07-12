package com.sanxing.sesame.jdbc.template.type;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DateTypeHandler
    implements TypeHandler
{
    @Override
    public Object getField( ResultSet rs, int index )
        throws SQLException
    {
        Timestamp value = rs.getTimestamp( index );
        if ( rs.wasNull() )
        {
            return null;
        }
        return new java.util.Date( value.getTime() );
    }

    @Override
    public void setParameter( PreparedStatement ps, int index, Object value )
        throws SQLException
    {
        Timestamp v = null;
        if (value instanceof Timestamp)
        {
            v = (Timestamp) value;
        }
        else
        {
            v = new Timestamp( ((Date) value).getTime() );
        }
        ps.setTimestamp( index, v );
    }
}