/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

/**
 * Abstract test case for sesame tests.
 */
public abstract class SesameTestSupport
    extends PlexusTestCaseSupport
{

    public static final String WORK_CONFIGURATION_KEY = "sesame-work";

    public static final String APPS_CONFIGURATION_KEY = "apps";

    public static final String CONF_DIR_KEY = "application-conf";

    public static final String SECURITY_XML_FILE = "security-xml-file";

    public static final String RUNTIME_CONFIGURATION_KEY = "runtime";

    public static final String SESAME_APP_CONFIGURATION_KEY = "sesame-app";

    private File plexusHomeDir = null;

    private File appsHomeDir = null;

    private File workHomeDir = null;

    private File confHomeDir = null;

    private File runtimeHomeDir = null;

    private File sesameappHomeDir = null;

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        plexusHomeDir = new File(
            getBasedir(), "target/plexus-home-" + new Random( System.currentTimeMillis() ).nextLong()
        );
        appsHomeDir = new File( plexusHomeDir, "apps" );
        workHomeDir = new File( plexusHomeDir, "sesame-work" );
        confHomeDir = new File( workHomeDir, "conf" );
        runtimeHomeDir = new File( plexusHomeDir, "runtime" );
        sesameappHomeDir = new File( plexusHomeDir, "sesame-app" );

        ctx.put( WORK_CONFIGURATION_KEY, workHomeDir.getAbsolutePath() );
        ctx.put( APPS_CONFIGURATION_KEY, appsHomeDir.getAbsolutePath() );
        ctx.put( CONF_DIR_KEY, confHomeDir.getAbsolutePath() );
        ctx.put( SECURITY_XML_FILE, getSesameSecurityConfiguration() );
        ctx.put( RUNTIME_CONFIGURATION_KEY, runtimeHomeDir.getAbsolutePath() );
        ctx.put( SESAME_APP_CONFIGURATION_KEY, sesameappHomeDir.getAbsolutePath() );
    }

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        configuration.setAutoWiring( true );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_CACHE );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        // keep since PlexusTestCase is not JUnit4 annotated
        super.setUp();

        // simply to make sure customizeContext is handled before anything else
        getContainer();

        plexusHomeDir.mkdirs();
        appsHomeDir.mkdirs();
        workHomeDir.mkdirs();
        confHomeDir.mkdirs();
        runtimeHomeDir.mkdirs();
        sesameappHomeDir.mkdirs();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        // keep since PlexusTestCase is not JUnit4 annotated
        super.tearDown();

        cleanDir( plexusHomeDir );
    }

    protected void cleanDir( File dir )
    {
        if ( dir != null )
        {
            try
            {
                FileUtils.deleteDirectory( dir );
            }
            catch ( IOException e )
            {
                // couldn't delete directory, too bad
            }
        }
    }

    public File getPlexusHomeDir()
    {
        return plexusHomeDir;
    }

    public File getWorkHomeDir()
    {
        return workHomeDir;
    }

    public File getConfHomeDir()
    {
        return confHomeDir;
    }

    protected String getSesameConfiguration()
    {
        return new File( confHomeDir, "sesame.xml" ).getAbsolutePath();
    }

    protected String getSecurityConfiguration()
    {
        return new File( confHomeDir, "security-configuration.xml" ).getAbsolutePath();
    }

    protected String getSesameSecurityConfiguration()
    {
        return new File( confHomeDir, "security.xml" ).getAbsolutePath();
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/sesame/default-oss-sesame.xml", getSesameConfiguration() );
    }

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/security/security.xml", getSesameSecurityConfiguration() );
    }

    protected void copyResource( String resource, String dest )
        throws IOException
    {
        InputStream stream = null;
        FileOutputStream ostream = null;
        try
        {
            stream = getClass().getResourceAsStream( resource );
            ostream = new FileOutputStream( dest );
            IOUtil.copy( stream, ostream );
        }
        finally
        {
            IOUtil.close( stream );
            IOUtil.close( ostream );
        }
    }

    protected void copyFromClasspathToFile( String path, String outputFilename )
        throws IOException
    {
        copyFromClasspathToFile( path, new File( outputFilename ) );
    }

    protected void copyFromClasspathToFile( String path, File output )
        throws IOException
    {
        FileUtils.copyStreamToFile( new RawInputStreamFacade( getClass().getResourceAsStream( path ) ), output );
    }

}
