package com.sanxing.sesame.jdbc.template.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import com.sanxing.sesame.jdbc.template.type.TypeHandler;
import com.sanxing.sesame.jdbc.template.type.TypeHandlerFactory;

public class MapParametersMapping
    implements ParametersMapping
{
    private Map<String, Object> parameters = null;

    private String[] paramNames = null;

    private Map<String, Integer> paramTypes = null;

    public MapParametersMapping( Map<String, Object> parameters, String[] paramNames )
    {
        this.parameters = parameters;
        this.paramNames = paramNames;
    }

    public MapParametersMapping( Map<String, Object> parameters, String[] paramNames, Map<String, Integer> paramTypes )
    {
        this.parameters = parameters;
        this.paramNames = paramNames;
        this.paramTypes = paramTypes;
    }

    @Override
    public void setParameters( PreparedStatement ps )
        throws SQLException
    {
        if ( parameters != null )
        {
            int paramLen = paramNames.length;
            if ( paramLen > 0 )
            {
                for ( int i = 0; i < paramLen; ++i )
                {
                    String paramName = paramNames[i];
                    try
                    {
                        int parameterIndex = i + 1;
                        Object paramValue = parameters.get( paramName );
                        if ( paramTypes == null )
                        {
                            if ( paramValue == null )
                            {
                                ps.setObject( parameterIndex, paramValue );
                            }
                            else
                            {
                                TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler( paramValue.getClass() );
                                typeHandler.setParameter( ps, parameterIndex, paramValue );
                                
                            }
                        }
                        else
                        {
                            int paramType = paramTypes.get( paramName ).intValue();
                            ps.setObject( parameterIndex, paramValue, paramType );
                        }
                    }
                    catch ( Exception e )
                    {
                        throw new DataAccessException( "parameter[" + paramName + "] error:" + e.getMessage(), e );
                    }
                }
            }
        }
    }
}