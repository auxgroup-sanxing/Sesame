package com.sanxing.sesame.jdbc.template.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import com.sanxing.sesame.jdbc.template.type.TypeHandler;
import com.sanxing.sesame.jdbc.template.type.TypeHandlerFactory;

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
                            if ( parameters[i] == null )
                            {
                                ps.setObject( parameterIndex, parameters[i] );
                            }
                            else
                            {
                                TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler( parameters[i].getClass() );
                                typeHandler.setParameter( ps, parameterIndex, parameters[i] );
                                
                            }
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