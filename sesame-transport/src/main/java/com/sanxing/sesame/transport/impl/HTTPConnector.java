package com.sanxing.sesame.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.jbi.messaging.MessagingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.ws.commons.schema.XmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.annotation.Description;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;

@Description( "超文本传输协议" )
public class HTTPConnector
    extends BaseTransport
    implements Connector
{
    private static Logger LOG = LoggerFactory.getLogger( HTTPConnector.class );

    private Map<?, ?> properties = new HashMap();

    private int timeout;

    private URI uri;

    private boolean isActive;

    @Override
    public synchronized void open()
        throws IOException
    {
        isActive = true;

        LOG.info( this + " opened" );
    }

    @Override
    public synchronized void close()
        throws IOException
    {
        isActive = false;
        
        LOG.info( "Transport closed: " + getURI() );
    }

    public XmlSchema getSchema()
    {
        return null;
    }

    public String getDescription()
    {
        return "HTTP 协议客户端";
    }

    public String getProperty( String key )
    {
        return ( (String) properties.get( key ) );
    }

    public int getProperty( String key, int def )
    {
        try
        {
            String value = getProperty( key );
            return ( ( value == null ) ? def : Integer.parseInt( value ) );
        }
        catch ( NumberFormatException e )
        {
        }
        return def;
    }

    @Override
    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
        this.properties = properties;
        timeout = getProperty( "timeout", 10 );
    }

    public static byte[] string2Bcd( String str )
    {
        if ( str.length() % 2 != 0 )
        {
            str = "0" + str;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        char[] carray = str.toCharArray();
        for ( int i = 0; i < carray.length; i += 2 )
        {
            int high = carray[i] - '0';
            int low = carray[( i + 1 )] - '0';
            output.write( high << 4 | low );
        }
        return output.toByteArray();
    }

    public static String bcd2String( byte[] bytes )
    {
        StringBuffer sbuf = new StringBuffer();
        for ( int i = 0; i < bytes.length; ++i )
        {
            int h = ( ( bytes[i] & 0xFF ) >> 4 ) + 48;
            sbuf.append( (char) h );
            int l = ( bytes[i] & 0xF ) + 48;
            sbuf.append( (char) l );
        }
        return sbuf.toString();
    }

    @Override
    public String toString()
    {
        return "{ name:'" + super.getClass().getSimpleName() + "', url: '" + getURI() + "' }";
    }

    @Override
    public void setConfig( String contextPath, Element config )
        throws IllegalArgumentException
    {
        Map properties = new HashMap();
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
                LOG.debug( properties.toString() );
                setProperties( properties );
            }
            catch ( XPathExpressionException e )
            {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        }
    }

    @Override
    public String getCharacterEncoding()
    {
        String encoding = getProperty( "encoding" );
        return ( ( encoding != null ) ? encoding : System.getProperty( "file.encoding" ) );
    }

    @Override
    public void sendOut( MessageContext context )
        throws IOException
    {
        String targetURL = getURI().toString();
        PostMethod post = new PostMethod( targetURL );
        try
        {
            BinarySource in = (BinarySource) context.getSource();
            BinaryResult out = (BinaryResult) context.getResult();

            if ( in.getBytes() != null )
            {
                byte[] temp = in.getBytes();
                int len = temp.length;
                InputStream stream = new ByteArrayInputStream( temp, 0, len );
                post.setRequestEntity( new InputStreamRequestEntity( stream ) );
            }

            post.setPath( context.getPath() );
            post.setRequestHeader( "HTTPAction", context.getAction() );
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout( timeout );
            LOG.debug( "post ...................................." );
            int status = client.executeMethod( post );

            LOG.debug( "status: " + status );
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( new String( post.getResponseBody(), getCharacterEncoding() ) );
            }
            if ( status == 200 )
            {
                LOG.debug( "post success" );

                out.write( post.getResponseBody(), 0, (int) post.getResponseContentLength() );
                out.setEncoding( getCharacterEncoding() );

                postMessage( context );
            }

            throw new IOException( "http error: " + status );
        }
        catch ( MessagingException e )
        {
            e.printStackTrace();
        }
        catch ( BindingException e )
        {
            e.printStackTrace();
        }
        finally
        {
            post.releaseConnection();
        }
        post.releaseConnection();
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public boolean isActive()
    {
        return isActive;
    }

    @Override
    public void setURI( URI uri )
    {
        this.uri = uri;
    }
}