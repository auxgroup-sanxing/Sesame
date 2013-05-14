package com.sanxing.sesame.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import javax.wsdl.xml.WSDLLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class WSDLLocatorImpl
    implements WSDLLocator
{
    private static Logger LOG = LoggerFactory.getLogger( WSDLLocatorImpl.class );

    private final URI baseURI;

    private URI latestImportURI;

    private final Map<URI, InputStream> openedResources = new Hashtable();

    public WSDLLocatorImpl( File file )
    {
        baseURI = file.toURI();
    }

    public WSDLLocatorImpl( String documentBaseURI )
        throws URISyntaxException
    {
        baseURI = new URI( documentBaseURI );
    }

    @Override
    public void close()
    {
        for ( Map.Entry entry : openedResources.entrySet() )
        {
            try
            {
                InputStream input = (InputStream) entry.getValue();
                input.close();
            }
            catch ( IOException localIOException )
            {
            }
        }
    }

    @Override
    public InputSource getBaseInputSource()
    {
        try
        {
            InputStream input = baseURI.toURL().openStream();
            openedResources.put( baseURI, input );
            InputSource inputSource = new InputSource( input );

            return inputSource;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public String getBaseURI()
    {
        return baseURI.toString();
    }

    @Override
    public InputSource getImportInputSource( String parentLocation, String importLocation )
    {
        try
        {
            URI importURI = new URI( importLocation );
            URI parentURI = new URI( parentLocation );
            if ( ( importURI.getScheme() == null ) && ( "file".equals( parentURI.getScheme() ) ) )
            {
                File parentFile = new File( parentURI );
                File importFile = new File( importLocation );
                if ( ( parentFile.getName().equals( "unit.wsdl" ) ) && ( importFile.getName().equals( "unit.wsdl" ) ) )
                {
                    String unitName = importFile.getParentFile().getName();

                    File sus = parentFile.getParentFile().getParentFile().getParentFile();
                    boolean found = false;
                    File[] list = sus.listFiles();
                    for ( File item : list )
                    {
                        if ( !( item.isDirectory() ) )
                        {
                            continue;
                        }
                        for ( File entry : item.listFiles() )
                        {
                            if ( entry.getName().equals( unitName ) )
                            {
                                latestImportURI = new File( entry, "unit.wsdl" ).toURI();
                                LOG.debug( "Import wsdl - " + latestImportURI );
                                found = true;
                                break;
                            }
                        }
                    }
                    if ( !( found ) )
                    {
                        latestImportURI = parentURI.resolve( importLocation );
                    }
                }
                else
                {
                    latestImportURI = parentURI.resolve( importLocation );
                }
            }
            else
            {
                latestImportURI = parentURI.resolve( importLocation );
            }
            InputStream input = openedResources.get( latestImportURI );
            if ( input == null )
            {
                input = latestImportURI.toURL().openStream();
                openedResources.put( latestImportURI, input );
            }
            InputSource inputSource = new InputSource( input );
            return inputSource;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public String getLatestImportURI()
    {
        return latestImportURI.toString();
    }
}