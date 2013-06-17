package com.sanxing.sesame.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.jbi.messaging.MessagingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import com.sanxing.sesame.binding.util.BCD8421;
import com.sanxing.sesame.util.HexBinary;

@Description( "基本传输控制协议" )
public class SocketConnector
    extends BaseTransport
    implements Connector
{
    private static Logger LOG = LoggerFactory.getLogger( SocketConnector.class );

    private static final int DEFAULT_BUFFER_CAPACITY = 4096;

    private Thread sendThread;

    private int buffer_size = DEFAULT_BUFFER_CAPACITY;

    private final ThreadLocal<byte[]> cache = new ThreadLocal();

    private Map<?, ?> properties;

    private ByteOrder byteOrder;

    private String headEncoding = "10";

    private boolean lengthIncludeHead = false;

    private int recvOffset = 0;

    private int sendOffset = 0;

    private int recvHeadLen = 4;

    private int sendHeadLen = 4;

    private int timeout;

    private String alignment = "N";

    private byte[] padding = new byte[0];

    private Socket socket;

    private boolean keepalive = false;

    private boolean active;

    private InetSocketAddress socketAddress;

    public SocketConnector()
    {
        Map properties = new HashMap();
        properties.put( "encoding", "GBK" );

        this.properties = properties;
    }

    @Override
    public synchronized void open()
        throws IOException
    {
        active = true;

        URI uri = getURI();
        String hostname = uri.getHost();
        socketAddress = new InetSocketAddress( hostname, uri.getPort() );

        LOG.info( this + " opened" );
    }

    @Override
    public synchronized void close()
        throws IOException
    {
        if ( sendThread != null )
        {
            sendThread.interrupt();
        }
        active = false;
        LOG.info( "Transport closed: " + getURI() );
    }

    public XmlSchema getSchema()
    {
        return null;
    }

    public String getDescription()
    {
        return "TCP 协议连接器";
    }

    private int read( InputStream input, byte[] buffer )
        throws IOException
    {
        return input.read( buffer );
    }

    private void read( InputStream input, byte[] buffer, int off, int len )
        throws IOException
    {
        len += off;
        int l;
        for ( int p = off; ( l = input.read( buffer, p, len - p ) ) < len - p; )
        {
            p += l;
        }
    }

    public String getProperty( String key )
    {
        return ( (String) properties.get( key ) );
    }

    public String getProperty( String key, String def )
    {
        String value = getProperty( key );
        return ( ( value == null ) ? def : value );
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
            throw new NullPointerException( key + ":" + e.getMessage() );
        }
    }

    private byte[] exportHead( int length )
        throws IOException
    {
        int count = length;
        if ( lengthIncludeHead )
        {
            count += sendHeadLen;
        }

        if ( headEncoding.equals( "2" ) )
        {
            ByteBuffer headBuf = ByteBuffer.allocate( sendHeadLen );
            headBuf.order( byteOrder );
            switch ( headBuf.capacity() )
            {
                case 2:
                    headBuf.putShort( new Integer( count ).shortValue() );
                    return headBuf.array();
                case 4:
                    headBuf.putInt( count );
                    return headBuf.array();
            }
            throw new IOException( "非法的消息首部长度" );
        }

        if ( headEncoding.equals( "10" ) )
        {
            String head = String.format( "%0" + sendHeadLen + "d", new Object[] { Integer.valueOf( count ) } );
            return head.getBytes( getCharacterEncoding() );
        }
        if ( headEncoding.equals( "16" ) )
        {
            String head = String.format( "%0" + sendHeadLen + "x", new Object[] { Integer.valueOf( count ) } );
            return head.getBytes( getCharacterEncoding() );
        }
        if ( headEncoding.equals( "20" ) )
        {
            String head = String.format( "%0" + ( sendHeadLen * 2 ) + "d", new Object[] { Integer.valueOf( count ) } );
            return BCD8421.encode( head, null, '0' );
        }
        if ( headEncoding.equals( "26" ) )
        {
            String head = String.format( "%0" + ( sendHeadLen * 2 ) + "x", new Object[] { Integer.valueOf( count ) } );
            return HexBinary.decode( head );
        }

        return new byte[0];
    }

    private int extractHead( InputStream input )
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
                    if ( input.read( headBuf.array() ) < 0 )
                    {
                        return -1;
                    }
                    count = headBuf.getShort();
                    break;
                case 4:
                    if ( input.read( headBuf.array() ) < 0 )
                    {
                        return -1;
                    }
                    count = headBuf.getInt();
                    break;
                default:
                    throw new IOException( "非法的消息首部长度" );
            }
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            return count;
        }
        if ( headEncoding.equals( "10" ) )
        {
            input.skip( recvOffset );
            byte[] headBuf = new byte[recvHeadLen];
            if ( input.read( headBuf ) < 0 )
            {
                return -1;
            }
            count = Integer.parseInt( new String( headBuf ) );
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            return count;
        }
        if ( headEncoding.equals( "16" ) )
        {
            input.skip( recvOffset );
            byte[] headBuf = new byte[recvHeadLen];
            if ( input.read( headBuf ) < 0 )
            {
                return -1;
            }
            count = Integer.parseInt( new String( headBuf ), 16 );
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            return count;
        }
        if ( headEncoding.equals( "20" ) )
        {
            input.skip( recvOffset );
            byte[] headBuf = new byte[recvHeadLen];
            input.read( headBuf );
            count = Integer.parseInt( BCD8421.decode( headBuf, null, '0' ) );
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            return count;
        }
        if ( headEncoding.equals( "26" ) )
        {
            input.skip( recvOffset );
            byte[] headBuf = new byte[recvHeadLen];
            input.read( headBuf );
            count = Integer.parseInt( HexBinary.encode( headBuf ), 16 );
            if ( lengthIncludeHead )
            {
                count -= recvHeadLen;
            }
            return count;
        }

        return -1;
    }

    public void write( OutputStream output, byte[] bytes, int length )
        throws IOException
    {
        byte[] head = exportHead( bytes.length );
        if ( head.length > 0 )
        {
            int total = bytes.length + head.length + sendOffset;
            byte[] sendBuf = allocate( total );

            if ( padding.length >= sendOffset )
            {
                System.arraycopy( padding, 0, sendBuf, 0, sendOffset );
            }
            else
            {
                System.arraycopy( padding, 0, sendBuf, 0, padding.length );
            }
            System.arraycopy( head, 0, sendBuf, sendOffset, head.length );
            System.arraycopy( bytes, 0, sendBuf, sendOffset + head.length, bytes.length );
            output.write( sendBuf, 0, total );
        }
        else
        {
            if ( padding.length >= sendOffset )
            {
                output.write( padding, 0, sendOffset );
            }
            else
            {
                output.write( padding );
                output.write( new byte[sendOffset - padding.length] );
            }

            output.write( bytes, 0, length );
        }
        output.flush();
    }

    @Override
    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
        this.properties = properties;

        buffer_size = getProperty( "buffer_size", 1024 );
        keepalive = getProperty( "keep-alive", "false" ).equals( "true" );
        timeout = ( getProperty( "timeout", 30 ) * 1000 );
        String endian = getProperty( "endian" );
        byteOrder =
            ( ( ( endian == null ) || ( endian.equals( "big" ) ) ) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN );
        if ( getProperty( "len_encoding" ) != null )
        {
            headEncoding = getProperty( "len_encoding" );
        }
        if ( getProperty( "len_include" ) != null )
        {
            lengthIncludeHead = getProperty( "len_include" ).equals( "I" );
        }
        try
        {
            if ( getProperty( "recv_len_begin" ) != null )
            {
                recvOffset = Integer.parseInt( getProperty( "recv_len_begin" ) );
            }
            if ( getProperty( "recv_len_end" ) != null )
            {
                recvHeadLen = ( Integer.parseInt( getProperty( "recv_len_end" ) ) - recvOffset );
            }
            if ( getProperty( "send_len_begin" ) != null )
            {
                sendOffset = Integer.parseInt( getProperty( "send_len_begin" ) );
            }
            if ( getProperty( "send_len_end" ) != null )
            {
                sendHeadLen = ( Integer.parseInt( getProperty( "send_len_end" ) ) - sendOffset );
            }
        }
        catch ( Throwable e )
        {
            if ( ( headEncoding != null ) && ( !( headEncoding.equals( "0" ) ) ) )
            {
                throw new IllegalArgumentException( "Get head length failure: " + e.getMessage() );
            }
        }
        alignment = getProperty( "len_align", "N" );
        padding = HexBinary.decode( getProperty( "len_fill", "" ) );
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
        if ( ( config != null ) && ( contextPath == null ) )
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
        return getProperty( "encoding" );
    }

    @Override
    public boolean getKeepAlive()
    {
        return keepalive;
    }

    @Override
    public void setKeepAlive( boolean on )
    {
        keepalive = on;
    }

    @Override
    public void sendOut( MessageContext context )
        throws BindingException, IOException
    {
        Socket socket = null;
        try
        {
            if ( keepalive )
            {
                if ( ( this.socket != null ) && ( this.socket.isConnected() ) )
                {
                    socket = this.socket;
                    socket.setSoTimeout( timeout );
                }
            }
            else if ( ( this.socket != null ) && ( !( this.socket.isClosed() ) ) )
            {
                try
                {
                    this.socket.shutdownOutput();
                    this.socket.shutdownInput();
                    this.socket.close();
                }
                catch ( SocketException e )
                {
                    //ignore
                }
                finally
                {
                    this.socket = null;
                }
            }
            if (socket == null)
            {
                long millis = System.currentTimeMillis();
                if (keepalive)
                    socket = this.socket = new Socket();
                else
                    socket = new Socket();
                socket.setKeepAlive( keepalive );
                socket.setReuseAddress( true );
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Attempt to connect " + socketAddress );
                }
                socket.connect( socketAddress, timeout );
                Long elapsed = Long.valueOf( System.currentTimeMillis() - millis );
                socket.setSoTimeout( timeout - elapsed.intValue() );
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Socket connected to " + socket.getInetAddress() + ":" + socket.getPort() );
                }
            }
        }
        catch ( SocketException e )
        {
            this.socket = null;
            socket.close();
            throw e;
        }
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Socket read timeout: " + socket.getSoTimeout() );
        }
        if ( socket.getSoTimeout() <= 0 )
        {
            Exception e = new TimeoutException( "Connect timeout exception" );
            throw new BindingException( e.getMessage(), e );
        }

        PipeLine pipeline = null;
        try
        {
            pipeline = new PipeLine( socket.getInputStream(), socket.getOutputStream() );
            BinaryResult result = new BinaryResult();
            result.setEncoding( getProperty( "encoding" ) );
            sendRecv( pipeline, (BinarySource) context.getSource(), result );
            context.setResult( result );

            postMessage( context );
        }
        catch ( MessagingException e )
        {
            LOG.error( e.getMessage(), e );
        }
        catch ( SocketException e )
        {
            this.socket = null;
            socket.close();
            throw e;
        }
        finally
        {
            if (pipeline != null)
            {
                pipeline.close();
            }
            if ( !( keepalive ) && socket != null )
            {
                socket.close();
            }
        }
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    public void sendRecv( PipeLine pipeline, BinarySource in, BinaryResult out )
        throws IOException
    {
        InputStream input = pipeline.getInput();
        OutputStream output = pipeline.getOutput();
        String charset = getProperty( "encoding" );
        try
        {
            byte[] bytes = in.getBytes();
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Send Data: " + new String( bytes, 0, bytes.length, charset ) );
            }

            write( output, bytes, bytes.length );

            int count = extractHead( input );
            byte[] recvBuf;
            if ( count == -1 )
            {
                recvBuf = allocate( buffer_size );
                count = read( input, recvBuf );
            }
            else
            {
                recvBuf = allocate( count );
                read( input, recvBuf, 0, count );
            }

            if ( count > 0 )
            {
                String stringBuf = new String( recvBuf, 0, count, charset );
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Recv Data: " + stringBuf );
                }
                out.write( recvBuf, 0, count );
                return;
            }

            throw new IOException( "Received data is empty" );
        }
        catch ( IOException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new IOException( e.getMessage() );
        }
    }

    private byte[] allocate( int length )
    {
        byte[] buf = cache.get();
        if ( ( buf != null ) && ( length <= buf.length ) )
        {
            LOG.info( "Use cached buffer, length: " + buf.length );
            return buf;
        }

        cache.set( buf = new byte[length] );
        return buf;
    }
}