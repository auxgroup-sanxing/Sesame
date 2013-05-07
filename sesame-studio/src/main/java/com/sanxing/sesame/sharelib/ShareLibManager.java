package com.sanxing.sesame.sharelib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ShareLibManager
{
    private String baseDir;

    private String name;

    private String description = "";

    private String version = "1.0";

    private String callbackClazz = "";

    private boolean parentFirst = true;

    private final List<String> pathElements = new LinkedList();

    String template =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<jbi version=\"1.0\" xmlns=\"http://java.sun.com/xml/ns/jbi\" xmlns:sn=\"http://www.sanxing.com/ns/sesame\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
            + "    <shared-library  classs-loader-delegation=\"parent-first\" version=\"1.0\">\n"
            + "        <identification>\n" + "            <name></name>\n"
            + "            <description></description>\n" + "        </identification>\n"
            + "        <shared-library-class-path></shared-library-class-path>\n"
            + "        <sn:callback-class></sn:callback-class>\n" + "    </shared-library>\n" + "</jbi>";

    public String getCallbackClazz()
    {
        return callbackClazz;
    }

    public List<String> getPathElements()
    {
        return pathElements;
    }

    public boolean isParentFirst()
    {
        return parentFirst;
    }

    public void setParentFirst( boolean parentFirst )
    {
        this.parentFirst = parentFirst;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getBaseDir()
    {
        return baseDir;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    public ShareLibManager( String baseDir, String name, String version )
    {
        this.baseDir = baseDir;
        this.name = name;
        this.version = version;
    }

    public ShareLibManager( String baseDir )
    {
        try
        {
            this.baseDir = baseDir;
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build( new File( this.baseDir, "jbi.xml" ) );
            Namespace jbiNs = Namespace.getNamespace( "http://java.sun.com/xml/ns/jbi" );
            Element eleShareLib = doc.getRootElement().getChild( "shared-library", jbiNs );

            setParentFirst( "parent-first".equals( eleShareLib.getAttributeValue( "classs-loader-delegation" ) ) );
            version = eleShareLib.getAttributeValue( "version" );
            Element eleIdentification = eleShareLib.getChild( "identification", jbiNs );

            name = eleIdentification.getChildText( "name", jbiNs );
            description = eleIdentification.getChildText( "description", jbiNs );
            Element classPaths = eleShareLib.getChild( "shared-library-class-path", jbiNs );

            List elePathEles = classPaths.getChildren();
            for ( int i = 0; i < elePathEles.size(); ++i )
            {
                Element elePath = (Element) elePathEles.get( i );
                pathElements.add( elePath.getText() );
            }
            callbackClazz =
                eleShareLib.getChildTextNormalize( "callback-class",
                    Namespace.getNamespace( "http://www.sanxing.com/ns/sesame" ) );
        }
        catch ( JDOMException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public void addPathElement( String path )
    {
        removePathElement( path );
        pathElements.add( path );
    }

    public void removePathElement( String path )
    {
        if ( pathElements.contains( path ) )
        {
            pathElements.remove( path );
        }
    }

    public void persistence()
    {
        try
        {
            SAXBuilder builder = new SAXBuilder();
            Namespace jbiNs = Namespace.getNamespace( "http://java.sun.com/xml/ns/jbi" );

            Document doc = builder.build( new StringReader( template ) );
            Element eleShareLib = doc.getRootElement().getChild( "shared-library", jbiNs );

            if ( !( parentFirst ) )
            {
                eleShareLib.setAttribute( "classs-loader-delegation", "self-first" );
            }

            eleShareLib.setAttribute( "version", version );
            Element eleIdentification = eleShareLib.getChild( "identification", jbiNs );

            eleIdentification.getChild( "name", jbiNs ).setText( getName() );
            eleIdentification.getChild( "description", jbiNs ).setText( getDescription() );

            Element classPaths = eleShareLib.getChild( "shared-library-class-path", jbiNs );

            for ( String path : pathElements )
            {
                Element pathElement = new Element( "path-element", jbiNs );
                pathElement.setText( path );
                classPaths.addContent( pathElement );
            }
            if ( !( callbackClazz.equals( "" ) ) )
            {
                Element pathElement = new Element( "callback-class", "http://www.sanxing.com/ns/sesame" );

                pathElement.setText( callbackClazz );
                eleShareLib.addContent( pathElement );
            }
            File file = new File( baseDir, "jbi.xml" );

            FileOutputStream fout = new FileOutputStream( file );

            XMLOutputter xmloutput = new XMLOutputter();
            Format format = Format.getPrettyFormat();
            xmloutput.setFormat( format );
            xmloutput.output( doc, fout );
            fout.flush();
        }
        catch ( JDOMException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public static void main( String[] args )
    {
        ShareLibManager sm = new ShareLibManager( "/home/wangzheng/temp" );
        sm.addPathElement( "lib/log41.jar" );
        sm.persistence();
    }
}