package com.sanxing.sesame.engine.action.beanshell;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BSCManager
{
    public static final Logger LOG = LoggerFactory.getLogger( BSCManager.class );

    private static Map<String, ThreadLocal<BeanShellContext>> cache = new ConcurrentHashMap();

    private static List<String> staticFunctionPaths = new LinkedList();

    public static BeanShellContext getInstance( String processName )
    {
        LOG.debug( "got bsc for process[" + processName + "]" );
        if ( cache.get( processName ) == null )
        {
            LOG.debug( "there are not thread local ,create it" );
            cache.put( processName, new ThreadLocal() );
        }
        else
        {
            LOG.debug( "there are thread local ,use it" );
        }
        ThreadLocal bscInThreadCache = cache.get( processName );
        if ( bscInThreadCache.get() == null )
        {
            LOG.debug( "there are not in thread ,create new " );
            BeanShellContext bsc = new BeanShellContext();
            for ( String path : staticFunctionPaths )
            {
                bsc.addStaticFuncPath( path );
            }
            bscInThreadCache.set( bsc );
        }
        else
        {
            LOG.debug( "there are already in thread ,reuse it" );
        }
        BeanShellContext bsc = (BeanShellContext) bscInThreadCache.get();
        LOG.debug( "bsc is " + bsc );
        return bsc;
    }

    public static BeanShellContext getInstanceForDebug( String processName )
    {
        BeanShellContext bsc = new BeanShellContext();
        for ( String path : staticFunctionPaths )
        {
            bsc.addStaticFuncPath( path );
        }
        return bsc;
    }

    public static void reset4Flow( String processName )
    {
        cache.remove( processName );
        cache.put( processName, new ThreadLocal() );
    }

    public static synchronized void regStaticFuncPath( String path )
    {
        staticFunctionPaths.add( path );
    }
}