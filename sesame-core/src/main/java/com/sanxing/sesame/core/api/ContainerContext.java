package com.sanxing.sesame.core.api;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.naming.InitialContext;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Env;

public class ContainerContext
{
    private static Map<String, ContainerContext> instances = new HashMap();

    private final MBeanServer mbeanServer;

    private final Map<Object, Object> context = new HashMap();

    private final Env env;

    private final InitialContext serverContext;

    private final String containerName;

    private ClassLoader containerCLassLoader = null;

    private final BaseServer server;

    public static ContainerContext getInstance( String containerName )
    {
        ContainerContext answer = instances.get( containerName );
        if ( answer == null )
        {
            throw new RuntimeException( "unkown container" );
        }
        return answer;
    }

    public ContainerContext( String containerName, Env env, InitialContext serverContext, MBeanServer mbeanServer,
                             BaseServer _server )
    {
        this.containerName = containerName;
        this.env = env;
        this.serverContext = serverContext;
        instances.put( containerName, this );
        this.mbeanServer = mbeanServer;
        server = _server;

        JarFileClassLoader classLoader =
            new JarFileClassLoader( new URL[0], Thread.currentThread().getContextClassLoader(), false, new String[0],
                new String[] { "java.", "javax." } );

        File libDir = new File( getContainerDir(), "lib" );
        File classesDir = new File( getContainerDir(), "classes" );

        classLoader.addJarDir( libDir );
        classLoader.addClassesDir( classesDir );
        containerCLassLoader = classLoader;
    }

    public void put( Object key, Object value )
    {
        context.put( key, value );
    }

    public Object get( Object key )
    {
        return context.get( key );
    }

    public Env getEnv()
    {
        return env;
    }

    public MBeanServer getMbeanServer()
    {
        return mbeanServer;
    }

    public InitialContext getServerJNDIContext()
    {
        return serverContext;
    }

    public File getContainerDir()
    {
        File file = new File( env.getHomeDir(), "work/containers/" + containerName );
        if ( !( file.exists() ) )
        {
            file.mkdirs();
            new File( file, "lib" ).mkdir();
            new File( file, "classes" ).mkdir();
        }
        return file;
    }

    public ClassLoader getContainerClassLoader()
    {
        return containerCLassLoader;
    }

    public String getContainerName()
    {
        return containerName;
    }

    public BaseServer getServer()
    {
        return server;
    }
}