package com.sanxing.studio.emu;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingUnitManager
    implements ServletContextListener
{
    private static final long serialVersionUID = -7080130244789625051L;

    private static BindingUnitManager instance;

    private final Logger LOG = LoggerFactory.getLogger( BindingUnitManager.class );

    private final Map<String, BindingUnit> units = Collections.synchronizedMap( new HashMap() );

    public static BindingUnitManager getInstance()
    {
        return instance;
    }

    public void init( String serviceUnitName, String serviceUnitRootPath, Map<?, ?> properties )
        throws Exception
    {
        File rootDir = new File( serviceUnitRootPath );
        try
        {
            BindingUnit exists = units.get( serviceUnitName );
            if ( exists != null )
            {
                exists.stop();
                exists.setProperties( properties );
                return;
            }

            String className = String.valueOf( properties.get( "emulator" ) );
            Class unitClass = Class.forName( className ).asSubclass( BindingUnit.class );

            Constructor constructor = unitClass.getConstructor( new Class[] { Map.class, File.class } );
            BindingUnit unit = (BindingUnit) constructor.newInstance( new Object[] { properties, rootDir } );
            unit.init();
            units.put( serviceUnitName, unit );
        }
        catch ( NoSuchMethodException e )
        {
            throw new Exception( "Binding unit constructor not found: " + e.getMessage() );
        }
        catch ( ClassNotFoundException e )
        {
            throw new Exception( "Binding unit class not found: " + e.getMessage() );
        }
        catch ( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            LOG.debug( t.getMessage(), t );
            throw new Exception( "Binding unit instantiation error: " + t.getMessage() );
        }
    }

    public void shutDown( String serviceUnitName )
        throws Exception
    {
        BindingUnit unit = units.get( serviceUnitName );
        if ( unit != null )
        {
            unit.shutDown();
            units.remove( serviceUnitName );
        }
    }

    public void start( String serviceUnitName )
        throws Exception
    {
        units.get( serviceUnitName ).start();
    }

    public void stop( String serviceUnitName )
        throws Exception
    {
        BindingUnit unit = units.get( serviceUnitName );
        if ( unit == null )
        {
            return;
        }
        unit.stop();
    }

    private void shutdownNow()
        throws Exception
    {
        Set<Map.Entry<String, BindingUnit>> entries = units.entrySet();
        for ( Map.Entry entry : entries )
        {
            BindingUnit unit = (BindingUnit) entry.getValue();
            unit.shutDown();
        }
    }

    public int getCurrentState( String serviceUnitName )
    {
        BindingUnit unit = units.get( serviceUnitName );
        if ( unit != null )
        {
            return unit.getCurrentState();
        }
        return 0;
    }

    public BindingUnit getUnit( String name )
    {
        return units.get( name );
    }

    @Override
    public void contextDestroyed( ServletContextEvent event )
    {
        LOG.debug( "Shutdown BindingUnitManager" );
        try
        {
            shutdownNow();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    @Override
    public void contextInitialized( ServletContextEvent event )
    {
        instance = this;
    }
}