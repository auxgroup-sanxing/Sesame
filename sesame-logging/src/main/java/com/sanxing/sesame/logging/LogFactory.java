package com.sanxing.sesame.logging;

import java.util.Hashtable;

import com.sanxing.sesame.logging.impl.LogFactoryImpl;

public abstract class LogFactory
{
    public static final String BINDING_LOG = "sesame.binding";

    public static final String APP_LOG = "sesame.application";

    public static final String REVERSE_LOG = "sesame.reverse";

    public static final String SYSTEM_LOG = "sesame.system";

    public static final String SENSOR_LOG = "sesame.system.sensor";

    protected static Hashtable<ClassLoader, LogFactory> factories = new Hashtable();

    protected static LogFactory nullClassLoaderFactory;

    public static LogFactory getFactory()
    {
        LogFactory factory = getCachedFactory( Thread.currentThread().getContextClassLoader() );
        if ( factory == null )
        {
            factory = new LogFactoryImpl();
            cacheFactory( Thread.currentThread().getContextClassLoader(), factory );
        }
        return factory;
    }

    public static Log getLog( String name )
    {
        return getFactory().getInstance( name );
    }

    private static LogFactory getCachedFactory( ClassLoader contextClassLoader )
    {
        LogFactory factory = null;

        if ( contextClassLoader == null )
        {
            factory = nullClassLoaderFactory;
        }
        else
        {
            factory = factories.get( contextClassLoader );
        }

        return factory;
    }

    private static void cacheFactory( ClassLoader classLoader, LogFactory factory )
    {
        if ( factory != null )
        {
            if ( classLoader == null )
            {
                nullClassLoaderFactory = factory;
            }
            else
            {
                factories.put( classLoader, factory );
            }
        }
    }

    public abstract Log getInstance( String paramString );
}