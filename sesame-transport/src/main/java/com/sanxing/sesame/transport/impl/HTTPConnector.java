package com.sanxing.sesame.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import com.sanxing.sesame.binding.assist.PipeLine;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;

@Description( "超文本传输协议" )
public class HTTPConnector
    extends BaseTransport
    implements Connector, Runnable
{
    private static Logger LOG = LoggerFactory.getLogger( HTTPConnector.class );

    private static final int DEFAULT_BUFFER_CAPACITY = 8192;

    private static final int DEFAULT_READ_LENGTH = 1024;

    private Thread sendThread;

    private final ThreadPoolExecutor poolExecutor;

    private final int buffer_capacity = 8192;

    private Map<?, ?> properties = new HashMap();

    private ByteOrder byteOrder;

    private String headEncoding;

    private boolean lengthIncludeHead;

    private int recvOffset;

    private int sendOffset;

    private int recvHeadLen;

    private int sendHeadLen;

    private int timeout;

    private URI uri;

    private boolean isActive;

    private InetSocketAddress socketAddress;

    private final List<MessageContext> contextCache;

    public HTTPConnector()
    {
        poolExecutor = new ThreadPoolExecutor( 10, 100, 10000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue( 100 ) );
        contextCache = new ArrayList();
    }

    @Override
    public synchronized void open()
        throws IOException
    {
        isActive = true;
        URI uri = getURI();

        String hostname = uri.getHost();
        socketAddress = new InetSocketAddress( hostname, uri.getPort() );

        poolExecutor.prestartCoreThread();
        sendThread = new Thread( this );
        sendThread.start();
        LOG.info( this + " opened" );
    }

    @Override
    public synchronized void close()
        throws IOException
    {
        if ( sendThread != null )
        {
            try
            {
                sendThread.interrupt();
                if ( poolExecutor.getActiveCount() > 0 )
                {
                    poolExecutor.awaitTermination( 5000L, TimeUnit.MILLISECONDS );
                }
                else
                {
                    poolExecutor.awaitTermination( 0L, TimeUnit.MILLISECONDS );
                }
            }
            catch ( InterruptedException e )
            {
                throw new IOException( e.getMessage() );
            }
        }
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

    public int read( InputStream input, byte[] bytes )
        throws IOException
    {
        int count = 0;
        if ( headEncoding.equals( "2" ) )
        {
            input.skip( recvOffset );
            ByteBuffer headBuf = ByteBuffer.allocate( recvHeadLen );
            headBuf.order( byteOrder );
            switch ( headBuf.capacity() )
            {
                case 2:
                    input.read( headBuf.array() );
                    count = headBuf.getShort();
                    break;
                case 4:
                    input.read( headBuf.array() );
                    count = headBuf.getInt();
                    break;
                default:
                    throw new IOException( "非法的消息首部长度" );
            }
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            int l;
            for ( int p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
            {
                p += l;
            }
        }
        else if ( headEncoding.equals( "10" ) )
        {
            input.skip( recvOffset );
            byte[] headBuf = new byte[recvHeadLen];
            input.read( headBuf );
            count = Integer.parseInt( new String( headBuf ) );
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            int l;
            for ( int p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
            {
                p += l;
            }
        }
        else if ( headEncoding.equals( "16" ) )
        {
            input.skip( recvOffset );
            byte[] headBuf = new byte[recvHeadLen];
            input.read( headBuf );
            count = Integer.parseInt( new String( headBuf ), 16 );
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            int l;
            for ( int p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
            {
                p += l;
            }
        }
        else if ( headEncoding.equals( "20" ) )
        {
            input.skip( recvOffset );
            byte[] headBuf = new byte[recvHeadLen];
            input.read( headBuf );
            count = Integer.parseInt( bcd2String( headBuf ) );
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            int l;
            for ( int p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
            {
                p += l;
            }
        }
        else
        {
            int p = 0;
            int l;
            while ( ( l = input.read( bytes, p, 1024 ) ) != -1 )
            {
                p += l;
            }
            count = p;
        }
        return count;
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

    public void write( OutputStream output, byte[] bytes, int length )
        throws IOException
    {
        output.write( new byte[sendOffset] );

        int count = length;
        if ( lengthIncludeHead )
        {
            count -= sendHeadLen;
        }
        ByteBuffer headBuf;
        if ( headEncoding.equals( "2" ) )
        {
            headBuf = ByteBuffer.allocate( sendHeadLen );
            headBuf.order( byteOrder );
            switch ( headBuf.capacity() )
            {
                case 2:
                    headBuf.putShort( new Integer( count ).shortValue() );
                    output.write( headBuf.array() );
                    break;
                case 4:
                    headBuf.putInt( count );
                    output.write( headBuf.array() );
                    break;
                default:
                    throw new IOException( "非法的消息首部长度" );
            }
        }
        else if ( headEncoding.equals( "10" ) )
        {
            String head = String.format( "%0" + sendHeadLen + "d", new Object[] { Integer.valueOf( count ) } );
            output.write( head.getBytes() );
        }
        else if ( headEncoding.equals( "16" ) )
        {
            String head = String.format( "%0" + sendHeadLen + "x", new Object[] { Integer.valueOf( count ) } );
            output.write( head.getBytes() );
        }
        else if ( headEncoding.equals( "20" ) )
        {
            String head = String.format( "%0" + ( sendHeadLen * 2 ) + "d", new Object[] { Integer.valueOf( count ) } );
            output.write( string2Bcd( head ) );
        }

        output.write( bytes, 0, length );
        output.flush();
    }

    @Override
    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
        this.properties = properties;
        timeout = getProperty( "timeout", 10 );
        String endian = getProperty( "endian" );
        byteOrder =
            ( ( ( endian == null ) || ( endian.equals( "big" ) ) ) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN );
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
            BinaryResult out = (BinaryResult) context.getResult();
            BinarySource in = (BinarySource) context.getSource();

            if ( out.getBytes() != null )
            {
                byte[] temp = out.getBytes();
                int len = temp.length;
                InputStream stream = new ByteArrayInputStream( temp, 0, len );
                post.setRequestEntity( new InputStreamRequestEntity( stream, "text/xml" ) );
            }

            post.setPath( context.getPath() );
            post.setRequestHeader( "SOAPAction", "\"\"" );
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

                in.setBytes( post.getResponseBody(), (int) post.getResponseContentLength() );
                in.setEncoding( getCharacterEncoding() );

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

    public void connected( PipeLine pipeline )
    {
        Random rand = new Random();
        try
        {
            MessageContext context;
            do
            {
                int index = rand.nextInt( contextCache.size() );
                context = contextCache.get( index );
            }
            while ( context == null );

            sendRecv( pipeline, context.getResult(), context.getSource() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    public void sendRecv( PipeLine pipeline, javax.xml.transform.Result out, javax.xml.transform.Source in )
        throws IOException
    {
        // TODO
    }

    @Override
    public void run()
    {
        int timeout = getProperty( "timeout", 30 ) * 1000;
        int interval = getProperty( "interval", 1 ) * 1000;
        while ( !( isActive ) )
        {
            try
            {
                if ( contextCache.size() > 0 )
                {
                    Socket socket = new Socket();
                    socket.connect( socketAddress, timeout );
                    LOG.debug( "Socket connected to " + socket.getInetAddress() + ":" + socket.getPort() );
                    PipeLine pipeline = new PipeLine( socket.getInputStream(), socket.getOutputStream() )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            getInput().close();
                            getOutput().close();
                        }
                    };
                    poolExecutor.execute( new Command( pipeline ) );
                }
                Thread.sleep( interval );
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
    }

    private class Command
        implements Runnable
    {
        PipeLine chnn;

        Command( PipeLine paramPipeLine )
        {
            chnn = paramPipeLine;
        }

        @Override
        public void run()
        {
            connected( chnn );
        }
    }
}