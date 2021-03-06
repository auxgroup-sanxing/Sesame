package com.sanxing.sesame.util;

import java.util.HashMap;
import java.util.Map;

import com.sanxing.sesame.exceptions.SystemException;

public class ServiceLocator
{
    private static Map<Class, Object> cache = new HashMap();

    public static <T> T getSerivce( Class<T> clazz )
    {
        if ( !( cache.containsKey( clazz ) ) )
        {
            try
            {
                String className = clazz.getName().replace( "api", "impl" );
                className = className + "Impl";
                cache.put( clazz, Class.forName( className ).newInstance() );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                SystemException se = new SystemException();
                se.setModuleName( "999" );
                se.setErrorCode( "99998" );
                throw se;
            }
        }
        return (T) cache.get( clazz );
    }
}