package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeHandler
    implements TypeHandler
{
    @Override
    public Object getField( ResultSet rs, int index )
        throws SQLException
    {
        Double value = null;
        double dValue = rs.getDouble( index );
        if ( !( rs.wasNull() ) )
        {
            value = new Double( dValue );
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
            Double v = (Double) value;
            ps.setDouble( index, v.doubleValue() );
        }
    }
}