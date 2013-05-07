package com.sanxing.studio.emu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;

import org.apache.ws.commons.schema.XmlSchema;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;

public class TcpClient
    extends BindingUnit
    implements Client
{
    private static final int DEFAULT_BUFFER_CAPACITY = 8192;

    protected static final Logger LOG = LoggerFactory.getLogger( TcpClient.class );

    private static final int DEFAULT_READ_LENGTH = 1024;

    private final Map<String, OperationEntry> consumeEntries = new Hashtable();

    private Thread sendThread;

    private final ThreadPoolExecutor poolExecutor;

    private int buffer_capacity = 8192;

    private ByteOrder byteOrder;

    private String headEncoding;

    private boolean lengthIncludeHead;

    private int recvOffset;

    private int sendOffset;

    private int recvHeadLen;

    private int sendHeadLen;

    private Class<? extends Decoder> decodeClass;

    private Class<? extends Encoder> encodeClass;

    private InetSocketAddress socketAddress;

    public TcpClient( Map<?, ?> properties, File serviceUnitRoot )
        throws Exception
    {
        super( properties, serviceUnitRoot );
        poolExecutor = new ThreadPoolExecutor( 10, 100, 10000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue( 100 ) );
        loadBundles();
    }

    @Override
    protected void setProperties( Map<?, ?> properties )
        throws Exception
    {
        super.setProperties( properties );
        byteOrder = ( ( getProperty( "endian" ).equals( "little" ) ) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN );
        headEncoding = getProperty( "len_encoding" );
        lengthIncludeHead = getProperty( "len_include" ).equals( "I" );
        try
        {
            recvOffset = Integer.parseInt( getProperty( "recv_len_begin" ) );
            recvHeadLen = ( Integer.parseInt( getProperty( "recv_len_end" ) ) - recvOffset );
            sendOffset = Integer.parseInt( getProperty( "send_len_begin" ) );
            sendHeadLen = ( Integer.parseInt( getProperty( "send_len_end" ) ) - sendOffset );
        }
        catch ( NumberFormatException e )
        {
            if ( !( headEncoding.equals( "0" ) ) )
            {
                throw new Exception( "Get head length failure: " + e.getMessage(), e );
            }
        }
        try
        {
            decodeClass = Class.forName( getProperty( "decode-class" ) ).asSubclass( Decoder.class );
            encodeClass = Class.forName( getProperty( "encode-class" ) ).asSubclass( Encoder.class );
        }
        catch ( ClassNotFoundException e )
        {
            throw new Exception( "Specified decode/encode class not found: " + e.getMessage(), e );
        }
        try
        {
            buffer_capacity = Integer.parseInt( getProperty( "buffer_size" ) );
        }
        catch ( Throwable e )
        {
            LOG.warn( "读取缓冲区大小失败，采用默认值" );
        }
    }

    protected void loadBundles()
        throws JDOMException, IOException, WSDLException
    {
        SAXBuilder builder = new SAXBuilder();
        File unitFile = new File( getUnitRoot(), "META-INF/unit.xml" );
        Element rootEl = builder.build( unitFile ).getRootElement();
        String oriented = rootEl.getAttributeValue( "oriented" );
        String endpoint = rootEl.getAttributeValue( "endpoint" );

        File epFolder = new File( getUnitRoot(), "../../" + oriented + "/" + endpoint );
        File wsdl = new File( epFolder, "META-INF/unit.wsdl" );
        Definition definition = loadDefition( wsdl );
        Map portTypes = definition.getPortTypes();

        String uri = definition.getTargetNamespace();
        for ( Iterator iter = portTypes.values().iterator(); iter.hasNext(); )
        {
            PortType intfEl = (PortType) iter.next();
            List opers = intfEl.getOperations();
            for ( Iterator it = opers.iterator(); it.hasNext(); )
            {
                Operation operEl = (Operation) it.next();
                String code = operEl.getName();
                OperationEntry entry = new OperationEntry();
                entry.setInterfaceName( intfEl.getQName().getLocalPart() );
                String namespace =
                    uri
                        + ( ( uri.endsWith( "/" ) ) ? code
                            : new StringBuilder().append( "/" ).append( code ).toString() );
                entry.setSchema( schemaForNamespace( namespace ) );

                File file = new File( getUnitRoot(), code + ".xml" );
                if ( file.isFile() )
                {
                    entry.setData( builder.build( file ) );
                }
                consumeEntries.put( code, entry );
            }
        }
    }

    public void send( byte[] buffer, InetSocketAddress address )
        throws Exception
    {
        Socket socket = new Socket( address.getHostName(), address.getPort() );
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        try
        {
            output.write( buffer );

            byte[] result = new byte[8192];
            int len = read( input, result );
            if ( len > 0 )
            {
                LOG.info( "received: " + new String( result, 0, len, getProperty( "encoding" ) ) );
            }
        }
        finally
        {
            input.close();
            output.close();
        }
    }

    @Override
    protected int read( InputStream input, byte[] bytes )
        throws IOException
    {
        int count = 0;
        int p;
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
            for ( p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
            {
                p += l;
            }
        }
        else
        {
            if ( headEncoding.equals( "10" ) )
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
                for ( p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
                {
                    p += l;
                }
            }
            else
            {
                if ( headEncoding.equals( "16" ) )
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
                    for ( p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
                    {
                        p += l;
                    }
                }
                else
                {
                    if ( headEncoding.equals( "20" ) )
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
                        for ( p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
                        {
                            p += l;
                        }
                    }
                    else
                    {
                        p = 0;
                        int l;
                        while ( ( l = input.read( bytes, p, 1024 ) ) != -1 )
                        {
                            p += l;
                        }
                        count = p;
                    }
                }
            }
        }
        return count;
    }

    @Override
    protected void write( OutputStream output, byte[] bytes, int length )
        throws IOException
    {
        output.write( new byte[sendOffset] );

        int count = length;
        if ( lengthIncludeHead )
        {
            count -= sendHeadLen;
        }

        if ( headEncoding.equals( "2" ) )
        {
            ByteBuffer headBuf = ByteBuffer.allocate( sendHeadLen );
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
    protected void dispose()
    {
        consumeEntries.clear();
        super.dispose();
    }

    @Override
    protected Decoder getDecoder()
        throws Exception
    {
        return decodeClass.newInstance();
    }

    @Override
    protected Encoder getEncoder()
        throws Exception
    {
        return encodeClass.newInstance();
    }

    @Override
    public void init()
        throws Exception
    {
        super.init();
        String hostname = String.valueOf( getProperty( "hostname" ) );
        int port = 0;
        try
        {
            port = Integer.parseInt( getProperty( "port" ) );
        }
        catch ( NumberFormatException e )
        {
            throw new Exception( "port must be a valid number" );
        }
        socketAddress = new InetSocketAddress( hostname, port );
    }

    @Override
    public void start()
        throws Exception
    {
        poolExecutor.prestartCoreThread();
        sendThread = new Thread( this );
        sendThread.start();
        super.start();
    }

    @Override
    public void stop()
        throws Exception
    {
        super.stop();
        if ( sendThread == null )
        {
            return;
        }
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
            LOG.debug( "PoolExecutor Terminated!" );
        }
        catch ( InterruptedException e )
        {
            throw new Exception( e );
        }
    }

    @Override
    public void shutDown()
        throws Exception
    {
        stop();
        if ( poolExecutor.getActiveCount() > 0 )
        {
            poolExecutor.shutdown();
        }
        else
        {
            poolExecutor.shutdownNow();
        }
        dispose();
    }

    @Override
    public void run()
    {
        int timeout = getProperty( "timeout", 30 ) * 1000;
        int interval = getProperty( "interval", 1 ) * 1000;
        while ( getCurrentState() == 1 )
        {
            try
            {
                Socket socket = new Socket();
                socket.connect( socketAddress, timeout );
                LOG.debug( "Socket connected to " + socket.getInetAddress() + ":" + socket.getPort() );

                StreamChannel chnn = new StreamChannel( socket.getInputStream(), socket.getOutputStream() )
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        getInput().close();
                        getOutput().close();
                    }
                };
                poolExecutor.execute( new Command( chnn ) );
                Thread.sleep( interval );
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
    }

    public void connected( StreamChannel channel )
    {
        Random rand = new Random();
        Object[] entries = consumeEntries.keySet().toArray();
        try
        {
            String code;
            OperationEntry entry;
            do
            {
                int index = rand.nextInt( entries.length );
                code = (String) entries[index];
                entry = consumeEntries.get( code );
            }
            while ( entry.getData() == null );

            Element rootEl = entry.getData().getRootElement();
            String selector = getProperty( "selector" );
            if ( selector.equals( "random()" ) )
            {
                Random r = new Random();
                selector = String.valueOf( r.nextInt( rootEl.getChildren().size() ) + 1 );
            }
            Element dataEl = (Element) XPath.selectSingleNode( rootEl, "request[" + selector + "]" );
            sendRecv( channel, code, dataEl );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    public Element sendRecv( StreamChannel channel, String code, Element dataEl )
        throws IOException
    {
        InputStream input = channel.getInput();
        OutputStream output = channel.getOutput();
        String charset = getProperty( "encoding" );
        try
        {
            OperationEntry entry = consumeEntries.get( code );
            if ( entry.getSchema() == null )
            {
                LOG.debug( entry + ": Schema not found" );
                return null;
            }
            byte[] sendBuf = new byte[buffer_capacity];
            int len = xml2raw( dataEl, entry.getSchema(), sendBuf );

            LOG.debug( "Send Data: " + new String( sendBuf, 0, len, charset ) );

            write( output, sendBuf, len );

            byte[] recvBuf = new byte[buffer_capacity];
            int length = read( input, recvBuf );
            String stringBuf;
            if ( length > 0 )
            {
                stringBuf = new String( recvBuf, 0, length, charset );
                LOG.debug( "Received Data: " + stringBuf );
                return raw2xml( recvBuf, length, entry.getSchema() );
            }

            throw new IOException( "Received Data is blank" );
        }
        catch ( IOException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
        }
        finally
        {
            channel.close();
        }
        return null;
    }

    @Override
    protected int xml2raw( Element dataEl, XmlSchema schema, byte[] result )
        throws Exception
    {
        dataEl.setName( "request" );
        return super.xml2raw( dataEl, schema, result );
    }

    protected Element raw2xml( byte[] buffer, int length, XmlSchema schema )
        throws Exception
    {
        return super.raw2xml( buffer, length, schema, "response" );
    }

    @Override
    public Element send( String code, Element data, int timeout )
        throws IOException
    {
        LOG.debug( "Connect " + socketAddress );
        Socket socket = new Socket();
        socket.connect( socketAddress, timeout );
        LOG.debug( "Socket connected to " + socket.getInetAddress() + ":" + socket.getPort() );

        StreamChannel chnn = new StreamChannel( socket.getInputStream(), socket.getOutputStream() )
        {
            @Override
            public void close()
                throws IOException
            {
                getInput().close();
                getOutput().close();
            }
        };
        return sendRecv( chnn, code, data );
    }

    private class Command
        implements Runnable
    {
        StreamChannel chnn;

        Command( StreamChannel paramStreamChannel )
        {
            chnn = paramStreamChannel;
        }

        @Override
        public void run()
        {
            connected( chnn );
        }
    }
}