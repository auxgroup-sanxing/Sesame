package com.sanxing.sesame.jdbc.template.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.ParametersMapping;

public class ArrayParametersMapping
    implements ParametersMapping
{
    private Object[] parameters = null;

    private int[] paramTypes = null;

    public ArrayParametersMapping( Object[] parameters )
    {
        this.parameters = parameters;
    }

    public ArrayParametersMapping( Object[] parameters, int[] paramTypes )
    {
        this.parameters = parameters;
        this.paramTypes = paramTypes;
    }

    @Override
    public void setParameters( PreparedStatement ps )
        throws SQLException
    {
        if ( parameters != null )
        {
            int len = parameters.length;
            if ( len > 0 )
            {
                for ( int i = 0; i < len; ++i )
                {
                    int parameterIndex = i + 1;
                    try
                    {
                        if ( paramTypes == null )
                        {
                            ps.setObject( parameterIndex, parameters[i] );
                        }
                        else
                        {
                            ps.setObject( parameterIndex, parameters[i], paramTypes[i] );
                        }
                    }
                    catch ( Exception e )
                    {
                        throw new DataAccessException( "parameter[" + String.valueOf( parameterIndex ) + "] error:"
                            + e.getMessage(), e );
                    }
                }
            }
        }
    }
}