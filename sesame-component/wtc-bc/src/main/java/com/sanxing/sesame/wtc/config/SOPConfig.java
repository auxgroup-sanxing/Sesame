package com.sanxing.sesame.wtc.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOPConfig
{
    private static Logger LOG = LoggerFactory.getLogger( SOPConfig.class );

    private SOPHeader header = new SOPHeader();

    private Map<String, SOPOperation> stores = null;

    private File storeFile;

    private static SOPConfig _instance;

    public static synchronized SOPConfig getInstance()
    {
        if ( _instance == null )
        {
            _instance = new SOPConfig();
        }
        return _instance;
    }

    public void addOperation( SOPOperation operation )
    {
        try
        {
            if ( !this.stores.containsKey( operation.getCode() ) )
            {
                this.header.addOperation( operation );
                this.stores.put( operation.getCode(), operation );
                persistence();
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public void delOperation( String code )
    {
        try
        {
            this.header.delOperation( code );
            this.stores.remove( code );
            persistence();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public SOPOperation getOperation( String code )
    {
        return (SOPOperation) this.stores.get( code );
    }

    public Map<String, SOPOperation> getOperations()
    {
        return this.stores;
    }

    public SOPHeader getHeader()
    {
        return this.header;
    }

    public int size()
    {
        return this.stores.size();
    }

    private SOPConfig()
    {
        init();
    }

    private void init()
    {
        try
        {
            URL schemaUrl = getClass().getClassLoader().getResource( "sopconfig.xml" );
            this.storeFile = new File( schemaUrl.getFile() );
            this.header = fromXML( this.storeFile );
            buildStore();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private void buildStore()
    {
        this.stores = new HashMap();
        ArrayList<SOPOperation> operations = this.header.getOperations();
        for ( SOPOperation operation : operations )
            this.stores.put( operation.getCode(), operation );
    }

    private void persistence()
        throws FileNotFoundException
    {
        toXML( this.header, this.storeFile );
    }

    private void toXML( SOPHeader header, File xmlFile )
    {
        try
        {
            FileOutputStream fos = new FileOutputStream( xmlFile );
            Element root = beanToElement( header );
            output( root, fos );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private void output( Element element, FileOutputStream fos )
    {
        try
        {
            XMLOutputter outputter = new XMLOutputter( Format.getPrettyFormat() );
            outputter.output( element, fos );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private Element beanToElement( SOPHeader header )
    {
        Element root = new Element( "header" );
        Element macbranch = new Element( "macbranch" );
        macbranch.addContent( header.getMacbranch() );
        Element channelfrom = new Element( "channelfrom" );
        channelfrom.addContent( header.getChannelfrom() );
        Element channelTo = new Element( "channelto" );
        channelTo.addContent( header.getChannelto() );
        Element identifier = new Element( "identifier" );
        identifier.addContent( header.getIdentifier() );

        root.addContent( macbranch );
        root.addContent( channelfrom );
        root.addContent( channelTo );
        root.addContent( identifier );

        Element operations = new Element( "operations" );
        ArrayList<SOPOperation> ops = header.getOperations();
        for ( SOPOperation operation : ops )
        {
            Element transaction = new Element( "transaction" );
            transaction.setAttribute( "code", operation.getCode() );
            transaction.setAttribute( "encrypt", operation.getEncrypt() );
            Element pinseed = new Element( "pinseed" );
            pinseed.addContent( operation.getPinseed() );
            Element pinflag = new Element( "pinflag" );
            pinflag.addContent( operation.getPinflag() );
            Element service = new Element( "service" );
            service.addContent( operation.getService() );
            transaction.addContent( pinseed );
            transaction.addContent( pinflag );
            transaction.addContent( service );

            operations.addContent( transaction );
        }
        root.addContent( operations );
        return root;
    }

    private SOPHeader elementToBean( Element root )
    {
        SOPHeader header = new SOPHeader();
        Element macbranch = root.getChild( "macbranch" );
        Element channelfrom = root.getChild( "channelfrom" );
        Element channelto = root.getChild( "channelto" );
        Element identifier = root.getChild( "identifier" );
        if ( macbranch != null )
        {
            header.setMacbranch( macbranch.getText() );
        }
        if ( channelfrom != null )
        {
            header.setChannelfrom( channelfrom.getText() );
        }
        if ( channelto != null )
        {
            header.setChannelto( channelto.getText() );
        }
        if ( identifier != null )
        {
            header.setIdentifier( identifier.getText() );
        }

        Element operations = root.getChild( "operations" );
        if ( operations != null )
        {
            ArrayList oplist = new ArrayList();
            List ops = operations.getChildren( "transaction" );
            for ( int i = 0; i < ops.size(); i++ )
            {
                SOPOperation operation = new SOPOperation();
                Element elm = (Element) ops.get( i );
                elm.getChild( "" );
                Attribute code = elm.getAttribute( "code" );
                Attribute encrypt = elm.getAttribute( "encrypt" );
                if ( code != null )
                {
                    operation.setCode( code.getValue() );
                }
                if ( encrypt != null )
                {
                    operation.setEncrypt( encrypt.getValue() );
                }
                Element pinseed = elm.getChild( "pinseed" );
                Element pinflag = elm.getChild( "pinflag" );
                Element service = elm.getChild( "service" );
                if ( pinseed != null )
                {
                    operation.setPinseed( pinseed.getText() );
                }
                if ( pinflag != null )
                {
                    operation.setPinflag( pinflag.getText() );
                }
                if ( service != null )
                {
                    operation.setService( service.getText() );
                }
                oplist.add( operation );
            }
            header.setOperations( oplist );
        }
        return header;
    }

    private SOPHeader fromXML( File xmlFile )
    {
        try
        {
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build( new FileInputStream( xmlFile ) );
            Element root = doc.getRootElement();
            return elementToBean( root );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }
}
