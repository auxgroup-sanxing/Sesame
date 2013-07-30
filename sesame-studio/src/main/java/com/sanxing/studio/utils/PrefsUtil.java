package com.sanxing.studio.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.sanxing.studio.Configuration;

public class PrefsUtil
{
    public static String getDescription( String project )
        throws IOException, JDOMException
    {
        File file = Configuration.getWorkspaceFile( project + "/jbi.xml" );
        if ( file.exists() )
        {
            SAXBuilder builder = new SAXBuilder();
            Element rootEl = builder.build( file ).getRootElement();
            Element saEl = rootEl.getChild("service-assembly", rootEl.getNamespace());
            if (saEl != null) {
                Element indenEl = saEl.getChild("identification", rootEl.getNamespace());
                String desc = indenEl.getChildText("description", rootEl.getNamespace());
                return ( ( desc != null ) ? desc : project );
            }
        }
        return project;
    }

    public static String getNamespaceUri( String project )
        throws IOException, JDOMException
    {
        File file = Configuration.getWorkspaceFile( project + "/jbi.xml" );
        if ( file.exists() )
        {
            SAXBuilder builder = new SAXBuilder();
            Element rootEl = builder.build( file ).getRootElement();
            Namespace ns = rootEl.getNamespace( "tns" );
            return ( ( ns != null ) ? ns.getURI() : "http://www.sanxing.com/ns/sesame/project/" );
        }
        return "http://www.sanxing.com/ns/sesame/project/";
    }

    public static Properties getPrefs( String project, String userId, String catalog )
        throws IOException, JDOMException
    {
        Properties properties = new Properties();
        File userFolder = Configuration.getWorkspaceFile( project + "/team/" + userId );
        if ( userFolder.exists() )
        {
            File file = new File( userFolder, catalog );
            if ( file.exists() )
            {
                InputStream input = new FileInputStream( file );
                try
                {
                    properties.load( input );
                }
                finally
                {
                    input.close();
                }
            }
        }
        return properties;
    }

    public static void savePrefs( String project, String userId, String catalog, Map<?, ?> map )
        throws IOException, JDOMException
    {
        Properties properties = new Properties();
        File userFolder = Configuration.getWorkspaceFile( project + "/team/" + userId );
        if ( userFolder.exists() )
        {
            File file = new File( userFolder, catalog );
            if ( file.exists() )
            {
                InputStream input = new FileInputStream( file );
                try
                {
                    properties.load( input );
                }
                finally
                {
                    input.close();
                }
            }
            properties.putAll( map );
            OutputStream output = new FileOutputStream( file );
            try
            {
                properties.store( output, "" );
            }
            finally
            {
                output.close();
            }
        }
    }
}