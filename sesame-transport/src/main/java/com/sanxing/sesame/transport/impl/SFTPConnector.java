package com.sanxing.sesame.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.transport.util.SesameSFTPClient;

public class SFTPConnector
    extends BaseTransport
    implements Connector
{
    private static Logger LOG = LoggerFactory.getLogger( SFTPConnector.class );

    private boolean active = false;

    public URI uri;

    public Map<?, ?> properties = new HashMap();

    protected Map<String, Map<String, Object>> bindingPropsCache = new HashMap();

    public String readLine;

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public void setURI( URI uri )
    {
        this.uri = uri;
    }

    public String getProperties( String key )
    {
        Object value = properties.get( key );
        return ( ( value == null ) ? "" : (String) value );
    }

    @Override
    public void setProperties( Map<?, ?> properties )
    {
        this.properties = properties;
        readLine = getProperties( "readLine" );
    }

    protected void write( OutputStream output, byte[] bytes, int length )
        throws IOException
    {
    }

    @Override
    public String getCharacterEncoding()
    {
        return "utf-8";
    }

    public String getDescription()
    {
        return "ftp get connector";
    }

    @Override
    public void close()
        throws IOException
    {
        active = false;
    }

    @Override
    public void open()
        throws IOException
    {
        active = true;
    }

    @Override
    public void sendOut( MessageContext context )
        throws IOException
    {
        String contextPath = context.getPath();
        Map bindingProps = bindingPropsCache.get( contextPath );
        SesameSFTPClient sftpClient = new SesameSFTPClient();
        try
        {
            sftpClient.setURI( uri );
            sftpClient.setProperties( properties );
            sftpClient.setBindingProperties( bindingProps );
            sftpClient.init();
            String model = (String) bindingProps.get( "model" );
            String filename = context.getAction();
            LOG.debug( "............... filename is:[" + filename + "],model is:" + model );

            if ( model.equals( "get" ) )
            {
                if ( sftpClient.moveFile( filename ) )
                {
                    InputStream in = sftpClient.processOneFile( filename );
                    byte[] buffer = sftpClient.getInputByte( in );
                    sftpClient.release();
                    LOG.debug( "Sftp cc get buffer is:[" + new String( buffer ) + "]" );

                    BinarySource source = new BinarySource();
                    source.setBytes( buffer, buffer.length );
                    source.setEncoding( getCharacterEncoding() );
                    context.setSource( source );
                }

            }
            else
            {
                String result = "failure";

                BinaryResult binaryResult = (BinaryResult) context.getResult();
                byte[] getBuf = binaryResult.getBytes();
                if ( getBuf != null )
                {
                    sftpClient.putFile( filename, getBuf );
                    sftpClient.release();
                    result = "success";
                }

                BinarySource source = new BinarySource();
                source.setBytes( result.getBytes(), result.getBytes().length );
                source.setEncoding( getCharacterEncoding() );
                context.setSource( source );
            }

            getCarrier( context ).post( context );
        }
        catch ( IOException e )
        {
            LOG.debug( e.getMessage(), e );
            throw e;
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public void setConfig( String contextPath, Element config )
        throws IllegalArgumentException
    {
        Map properties = new HashMap();

        if ( contextPath == null )
        {
            if ( config != null )
            {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                try
                {
                    String expression = "*/*";
                    NodeList nodes = (NodeList) xpath.evaluate( expression, config, XPathConstants.NODESET );
                    int i = 0;
                    for ( int len = nodes.getLength(); i < len; ++i )
                    {
                        Element prop = (Element) nodes.item( i );
                        properties.put( prop.getNodeName(), prop.getTextContent().trim() );
                    }
                }
                catch ( XPathExpressionException e )
                {
                    throw new IllegalArgumentException( e.getMessage(), e );
                }
            }
            LOG.debug( " .............SET root properties is:" + properties );
            setProperties( properties );
        }
        else
        {
            NodeList list = config.getChildNodes();
            for ( int i = 0; i < list.getLength(); ++i )
            {
                Node child = list.item( i );
                if ( child.getNodeType() == 1 )
                {
                    properties.put( child.getNodeName(), child.getTextContent().trim() );
                }
            }
            bindingPropsCache.put( contextPath, properties );
            LOG.debug( ".....put unit props is:" + properties + ",path is:" + contextPath );
        }
    }
}