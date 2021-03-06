package com.sanxing.studio;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.Platform;
import com.sanxing.studio.team.SCM;

public class Application
    implements ServletContextListener
{
    public static final String ENCRYPT_ALGORITHM = "RSA";

    public static final int KEY_SIZE = 512;

    private static final Logger LOG = LoggerFactory.getLogger( Application.class );

    private static ServletContext servletContext = null;
    
    private static File rootFolder;
    
    private static File rootLib;

    private static File projectFolder;

    private static File wareFolder;

    private static KeyPair keyPair;

    private static ServletContext getServletContext()
    {
        if ( servletContext == null )
        {
            throw new RuntimeException( "Application does not Initialized" );
        }
        return servletContext;
    }

    public static String getRealPath( String path )
    {
        return getServletContext().getRealPath( path );
    }

    public static File getSystemRoot()
    {
        return rootFolder;
    }

    public static File getSystemLib()
    {
        return rootLib;
    }

    public static File getSystemFile( String path )
    {
        return new File( getSystemRoot(), path );
    }

    public static File getWarehouseRoot()
    {
        return wareFolder;
    }

    public static File getWarehouseFile( String path )
    {
        return new File( getWarehouseRoot(), path );
    }

    public static File getWorkspaceRoot()
    {
        return projectFolder;
    }

    public static File getWorkspaceFile( String path )
    {
        return new File( getWorkspaceRoot(), path );
    }

    public static KeyPair getKeyPair()
    {
        return keyPair;
    }

    @Override
    public void contextDestroyed( ServletContextEvent event )
    {
        servletContext = null;
        SQLDataSource.closeDataSource();
        SCM.cleanUp();
    }

    @Override
    public void contextInitialized( ServletContextEvent event )
    {
        servletContext = event.getServletContext();
        
        servletContext.setAttribute( "MBeanServer", Platform.getLocalMBeanServer() );

        String home = System.getProperty( "SESAME_HOME" );
        rootFolder = new File( home );
        rootLib = new File( home, "lib" );
        projectFolder = new File( home, "projects" );
        if ( !( projectFolder.exists() ) )
        {
            projectFolder.mkdir();
        }

        wareFolder = new File( home, "warehouse" );
        if ( !( wareFolder.exists() ) )
        {
            wareFolder.mkdir();
        }

        try
        {
            KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA" );
            generator.initialize( 512 );
            keyPair = generator.generateKeyPair();
        }
        catch ( NoSuchAlgorithmException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }
}