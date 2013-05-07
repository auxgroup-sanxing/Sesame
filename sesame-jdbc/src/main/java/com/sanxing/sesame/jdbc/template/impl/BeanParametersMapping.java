package com.sanxing.sesame.jdbc.template.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import com.sanxing.sesame.jdbc.template.type.TypeHandler;
import com.sanxing.sesame.jdbc.template.type.TypeHandlerFactory;
import com.sanxing.sesame.util.BeanUtil;

public class BeanParametersMapping
    implements ParametersMapping
{
    private Object parameters = null;

    private String[] paramNames = null;

    public BeanParametersMapping( Object parameters, String[] paramNames )
    {
        this.parameters = parameters;
        this.paramNames = paramNames;
    }

    @Override
    public void setParameters( PreparedStatement ps )
        throws SQLException
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
                    Class paramClazz = BeanUtil.getPropertyType( parameters, paramName );

                    if ( paramClazz == null )
                    {
                        throw new DataAccessException( "paramName :[" + paramName + "] not in paramameters ["
                            + parameters + "]" );
                    }

                    TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler( paramClazz );
                    Object paramValue = BeanUtil.getProperty( parameters, paramName );
                    typeHandler.setParameter( ps, parameterIndex, paramValue );
                }
                catch ( Exception e )
                {
                    throw new DataAccessException( "parameter[" + paramName + "] error:" + e.getMessage(), e );
                }
            }
        }
    }
}