package com.sanxing.sesame.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.ws.commons.schema.XmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.binding.annotation.Description;
import com.sanxing.sesame.binding.assist.BlockedAcceptor;
import com.sanxing.sesame.binding.assist.PipeLine;
import com.sanxing.sesame.binding.util.BCD8421;
import com.sanxing.sesame.util.HexBinary;

@Description( "基本传输控制协议" )
public class SocketAcceptor
    extends BlockedAcceptor
{
    private static final Logger LOG = LoggerFactory.getLogger( SocketAcceptor.class );

    private static final int BUFFER_LIMIT = 8388608;

    private ServerSocket serverSocket;

    private Map<?, ?> properties = new HashMap();

    private int buffer_size = 1024;

    private final ThreadLocal<byte[]> cache = new ThreadLocal();

    private ByteOrder byteOrder;

    private String headEncoding = "10";

    private boolean lengthIncludeHead = false;

    private int recvOffset = 0;

    private int sendOffset = 0;

    private int recvHeadLen = 4;

    private int sendHeadLen = 4;

    private String alignment = "N";

    private byte[] padding = new byte[0];

    private int timeout = 30000;

    @Override
    public PipeLine accept()
        throws IOException
    {
        final Socket socket = serverSocket.accept();
        LOG.debug( "Accepted: " + socket.getRemoteSocketAddress() );
        socket.setSoTimeout( timeout );
        PipeLine pipeline = new PipeLine( socket.getInputStream(), socket.getOutputStream() )
        {

            @Override
            public void close()
                throws IOException
            {
                super.close();
                socket.close();
            }

            @Override
            public byte[] exportHead( int length )
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
                        case 3:
                    }
                    throw new IOException( "非法的消息首部长度" );
                }

                if ( headEncoding.equals( "10" ) )
                {
                    String head = String.format( "%0" + sendHeadLen + "d", new Object[] { Integer.valueOf( count ) } );
                    return head.getBytes( SocketAcceptor.this.getCharacterEncoding() );
                }
                if ( headEncoding.equals( "16" ) )
                {
                    String head = String.format( "%0" + sendHeadLen + "x", new Object[] { Integer.valueOf( count ) } );
                    return head.getBytes( SocketAcceptor.this.getCharacterEncoding() );
                }
                if ( headEncoding.equals( "20" ) )
                {
                    String head =
                        String.format( "%0" + ( sendHeadLen * 2 ) + "d", new Object[] { Integer.valueOf( count ) } );
                    return BCD8421.encode( head, null, '0' );
                }
                if ( headEncoding.equals( "26" ) )
                {
                    String head =
                        String.format( "%0" + ( sendHeadLen * 2 ) + "x", new Object[] { Integer.valueOf( count ) } );
                    return HexBinary.decode( head );
                }

                return new byte[0];
            }

            @Override
            public int extractHead()
                throws IOException
            {
                try
                {
                    InputStream input = getInput();
                    if ( !( headEncoding.equals( "0" ) ) )
                    {
                        input.skip( recvOffset );
                    }
                    int count = 0;
                    if ( headEncoding.equals( "2" ) )
                    {
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
                catch ( NumberFormatException e )
                {
                    throw new IllegalArgumentException( "非法报文头长度，长度编码方式[" + headEncoding + "]", e );
                }
            }

            @Override
            public void read( byte[] buffer, int off, int len )
                throws IOException
            {
                len += off;
                int l;
                for ( int p = off; ( l = getInput().read( buffer, p, len - p ) ) < len - p; )
                {
                    p += l;
                }
            }

            @Override
            public void write( byte[] bytes, int off, int length )
                throws IOException
            {
                OutputStream output = getOutput();

                byte[] head = exportHead( bytes.length );
                if ( head.length > 0 )
                {
                    int total = sendOffset + head.length + bytes.length;
                    byte[] sendBuf = SocketAcceptor.this.allocate( total );
                    if ( sendOffset > 0 )
                    {
                        if ( sendOffset <= padding.length )
                        {
                            System.arraycopy( padding, 0, sendBuf, 0, sendOffset );
                        }
                        else
                        {
                            System.arraycopy( padding, 0, sendBuf, 0, padding.length );
                        }
                    }
                    System.arraycopy( head, 0, sendBuf, sendOffset, head.length );
                    System.arraycopy( bytes, off, sendBuf, sendOffset + head.length, bytes.length );
                    output.write( sendBuf, 0, total );
                }
                else
                {
                    output.write( bytes, 0, length );
                }
                output.flush();
            }
        };
        return pipeline;
    }

    @Override
    public synchronized void open()
        throws IOException
    {
        URI uri = getURI();
        String hostname = uri.getHost();
        if ( hostname.equals( "" ) )
        {
            serverSocket = new ServerSocket( uri.getPort() );
        }
        else
        {
            InetSocketAddress socketAddress = new InetSocketAddress( hostname, uri.getPort() );
            serverSocket = new ServerSocket();
            serverSocket.bind( socketAddress );
        }
        LOG.info( "Listening at " + serverSocket.getLocalSocketAddress() );

        super.open();
    }

    @Override
    public synchronized void close()
        throws IOException
    {
        super.close();

        if ( serverSocket != null )
        {
            serverSocket.close();
            serverSocket = null;
        }
        LOG.info( "Transport closed: " + getURI() );
    }

    public XmlSchema getSchema()
    {
        return null;
    }

    public String getDescription()
    {
        return "TCP 协议服务器";
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
            throw new NullPointerException( key + ": " + e.getMessage() );
        }
    }

    @Override
    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
        this.properties = properties;

        buffer_size = getProperty( "buffer_size", 1024 );

        boolean keepalive = getProperty( "keep-alive", "false" ).equals( "true" );
        setKeepAlive( keepalive );

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
    protected byte[] allocate( int length )
    {
        byte[] buf = cache.get();
        if ( ( buf != null ) && ( length <= buf.length ) )
        {
            return buf;
        }

        if ( ( length < 0 ) || ( length > 8388608 ) )
        {
            throw new IllegalArgumentException( "Buffer limit is 8388608B, can not allocate " + length + " bytes" );
        }
        cache.set( buf = new byte[length] );
        return buf;
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
        String encoding = getProperty( "encoding" );
        return ( ( encoding != null ) ? encoding : System.getProperty( "file.encoding" ) );
    }

    @Override
    protected int getBufferSize()
    {
        return buffer_size;
    }
}