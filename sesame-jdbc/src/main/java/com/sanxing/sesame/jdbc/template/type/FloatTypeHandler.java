package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatTypeHandler
    implements TypeHandler
{
    @Override
    public Object getField( ResultSet rs, int index )
        throws SQLException
    {
        Float value = null;
        float lValue = rs.getFloat( index );
        if ( !( rs.wasNull() ) )
        {
            value = new Float( lValue );
        }
        return value;
    }

    @Override
    public void setParameter( PreparedStatement ps, int index, Object value )
        throws SQLException
    {
        if ( value == null )
        {
            ps.setNull( index, -5 );
        }
        else
        {
            Float v = (Float) value;
            ps.setFloat( index, v.floatValue() );
        }
    }
}