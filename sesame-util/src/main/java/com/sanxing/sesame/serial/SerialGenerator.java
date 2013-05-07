package com.sanxing.sesame.serial;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SerialGenerator
{
    public static final String SERIAL_GENERATOR = "sesame.platfrom.serial.generator";

    private static final Logger LOG = LoggerFactory.getLogger( SerialGenerator.class );

    private static SerialGenerator generator;

    private static AtomicLong serial = new AtomicLong( 1L );

    public static void initialize()
    {
        String className = System.getProperty( "sesame.platfrom.serial.generator" );
        try
        {
            if ( className != null )
            {
                LOG.debug( Thread.currentThread().getContextClassLoader().toString() );
                Class clazz = Class.forName( className, true, Thread.currentThread().getContextClassLoader() );
                initialize( clazz.asSubclass( SerialGenerator.class ) );
                return;
            }

            className = NonceGenerator.class.getName();
            initialize( NonceGenerator.class );
        }
        catch ( Exception e )
        {
            LOG.error( "Serial generator (" + className + ") initialize error", e );
        }
    }

    public static void initialize( Class<? extends SerialGenerator> clazz )
        throws InstantiationException, IllegalAccessException
    {
        generator = clazz.newInstance();
        serial.set( generator.allocate() );
        LOG.info( "------------------------------------------------------------------------------------------" );
        LOG.info( "Generator class: " + clazz.getName() + ", Initial serial no: " + serial );
        LOG.info( "------------------------------------------------------------------------------------------" );
    }

    public static long getSerial()
    {
        if ( generator == null )
        {
            throw new IllegalStateException( "Serial generator not initialized" );
        }

        synchronized ( serial )
        {
            long sn = serial.getAndIncrement();
            if ( sn + 1L > generator.getLimit() )
            {
                serial.set( generator.allocate() );
                sn = serial.getAndIncrement();
            }
            return sn;
        }
    }

    public abstract long allocate();

    public abstract long getLimit();
}