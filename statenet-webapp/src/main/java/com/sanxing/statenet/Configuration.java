/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.statenet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ShangjieZhou
 */
public class Configuration
    implements ServletContextListener
{
    private static final Logger LOG = LoggerFactory.getLogger( Configuration.class );

    private static ServletContext servletContext = null;
    
    private static Properties properties = null;
    
    /**
     * @return ServletContext
     */
    public static ServletContext getServletContext()
    {
        return servletContext;
    }
    
    /**
     * @return Properties
     */
    public static Properties getProperties()
    {
        return properties;
    }

    /*(non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed( ServletContextEvent event )
    {
        properties = null;
        servletContext = null;
    }

    /*(non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized( ServletContextEvent event )
    {
        servletContext = event.getServletContext();
        properties = new Properties();
        InputStream in = null;
        try
        {
            in = new FileInputStream( new File( servletContext.getRealPath( "WEB-INF/config.properties" ) ) );
            properties.load( in );
        }
        catch ( IOException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }

}
