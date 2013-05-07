package com.sanxing.sesame.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
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
import com.sanxing.sesame.transport.util.SesameFTPClient;

public class FtpConnector
    extends BaseTransport
    implements Connector
{
    private static Logger LOG = LoggerFactory.getLogger( FtpConnector.class );

    public URI uri;

    public Map<?, ?> properties = new HashMap();

    protected boolean active = false;

    protected Map<String, Map<String, Object>> bindingPropsCache = new HashMap();

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
        if ( context.getProperty( "command" ) != null )
        {
            executeCmd( context );
            return;
        }

        String contextPath = context.getPath();
        Map bindingProps = bindingPropsCache.get( contextPath );
        if ( bindingProps == null )
        {
            bindingProps = new HashMap();
        }

        SesameFTPClient ftpClient = new SesameFTPClient();
        try
        {
            ftpClient.setURI( uri );
            ftpClient.setProperties( properties );
            ftpClient.setBindingProperties( bindingProps );
            if ( !( ftpClient.connectAddLogin() ) )
            {
                throw new IOException( "Can not connect and login to ftp server" );
            }
            String filename = context.getAction();
            String model = (String) bindingProps.get( "model" );
            LOG.debug( "...............  filename is:[" + filename + "],model is:" + model );
            if ( model.equals( "get" ) )
            {
                if ( ftpClient.moveFile( filename ) )
                {
                    InputStream in = ftpClient.processOneFile( filename );
                    byte[] buffer = ftpClient.getInputByte( in );
                    LOG.debug( "ftp client get buffer is:[" + new String( buffer ) + "]" );

                    ftpClient.disconnect();

                    BinarySource source = new BinarySource();
                    source.setBytes( buffer, buffer.length );
                    source.setEncoding( getCharacterEncoding() );
                    context.setSource( source );
                }
                else
                {
                    LOG.warn( "can not move file:" + filename );
                    throw new IOException( "get file error!" );
                }
            }

            String result = "failure";

            BinaryResult binaryResult = (BinaryResult) context.getResult();
            byte[] getBuf = binaryResult.getBytes();
            if ( getBuf != null )
            {
                ByteArrayInputStream in = new ByteArrayInputStream( getBuf );
                if ( ftpClient.uploadFile( filename, in ) )
                {
                    result = "success";
                }
                else
                {
                    LOG.warn( "put file:[" + filename + "] error!" );
                    throw new IOException( "put file error!" );
                }
                in.close();
                ftpClient.disconnect();
            }

            BinarySource source = new BinarySource();
            source.setBytes( result.getBytes(), result.getBytes().length );
            source.setEncoding( getCharacterEncoding() );
            context.setSource( source );

            getCarrier( context ).post( context );
            // LOG.error("can not get listener by contextPath:[" + contextPath +
            // "]");
        }
        catch ( IOException ioe )
        {
            LOG.debug( ioe.getMessage(), ioe );
            throw ioe;
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
            throw new IOException( e.getMessage() );
        }
        finally
        {
            ftpClient.disconnect();
        }
    }

    private void executeCmd( MessageContext context )
        throws IOException
    {
        String contextPath = context.getPath();
        Map bindingProps = bindingPropsCache.get( contextPath );
        if ( bindingProps == null )
        {
            bindingProps = new HashMap();
        }

        SesameFTPClient ftpClient = new SesameFTPClient();
        try
        {
            ftpClient.setURI( uri );
            ftpClient.setProperties( properties );
            ftpClient.setBindingProperties( bindingProps );
            if ( !( ftpClient.connectAddLogin() ) )
            {
                throw new IOException( "Can not connect and login to FTP server: " + uri );
            }
            String command = context.getAction().toUpperCase();
            String filename = context.getPath();
            if ( command.equals( "GET" ) )
            {
                InputStream in = ftpClient.processOneFile( filename );
                byte[] buffer = ftpClient.getInputByte( in );
                LOG.debug( "ftp client get buffer is:[" + new String( buffer ) + "]" );

                ftpClient.disconnect();

                BinarySource source = new BinarySource();
                source.setBytes( buffer, buffer.length );
                source.setEncoding( getCharacterEncoding() );
                context.setSource( source );
            }
            else if ( command.equals( "PUT" ) )
            {
                Source source = context.getSource();
                LOG.debug( "PUT " + source.getSystemId() );

                URL url = new URL( source.getSystemId() );
                InputStream input = url.openStream();
                try
                {
                    if ( !( ftpClient.uploadFile( filename, input ) ) )
                    {
                        throw new IOException( "Put file '" + filename + "' failure!" );
                    }
                    ftpClient.disconnect();
                }
                finally
                {
                    input.close();
                }

                if ( context.getResult() == null )
                {
                    context.setResult( new StreamResult( source.getSystemId() ) );
                }
                else if ( context.getResult() instanceof BinaryResult )
                {
                    BinaryResult binResult = (BinaryResult) context.getResult();
                    binResult.getOutputStream().write( "200OK".getBytes( getCharacterEncoding() ) );
                }

            }
            else
            {
                Exception exception = new UnsupportedOperationException( command );
                context.setException( exception );
                context.setStatus( MessageContext.Status.FAULT );
            }
            getCarrier( context ).post( context );
        }
        catch ( IOException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            IOException ioe = new IOException( e.getMessage() );
            ioe.initCause( e );
            throw ioe;
        }
        finally
        {
            ftpClient.disconnect();
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