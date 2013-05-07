package com.sanxing.studio.emu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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

public class TcpServer
    extends BindingUnit
    implements Server
{
    private static final int DEFAULT_BUFFER_CAPACITY = 8192;

    private static final int DEFAULT_READ_LENGTH = 1024;

    protected static Logger LOG = LoggerFactory.getLogger( TcpServer.class );

    private final Map<String, OperationEntry> provideEntries = new Hashtable();

    private ServerSocket transport;

    private Thread acceptThread;

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

    public TcpServer( Map<?, ?> properties, File serviceUnitRoot )
        throws Exception
    {
        super( properties, serviceUnitRoot );
        poolExecutor = new ThreadPoolExecutor( 10, 1000, 10000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue( 10 ) );
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

        PortType intfEl;
        Iterator it;
        String uri = definition.getTargetNamespace();
        for ( Iterator iter = portTypes.values().iterator(); iter.hasNext(); )
        {
            intfEl = (PortType) iter.next();
            List opers = intfEl.getOperations();
            for ( it = opers.iterator(); it.hasNext(); )
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
                provideEntries.put( code, entry );
            }
        }
    }

    @Override
    public void run()
    {
        LOG.debug( "listening at: " + socketAddress );
        while ( ( transport != null ) && ( !( transport.isClosed() ) ) )
        {
            try
            {
                Socket socket = transport.accept();
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
            }
            catch ( Exception e )
            {
                if ( transport == null )
                {
                    break;
                }
                if ( !( transport.isClosed() ) )
                {
                    LOG.error( e.getMessage(), e );
                }
            }
        }
        LOG.info( "transport closed: " + transport );
    }

    protected void accepted( StreamChannel channel )
    {
        InputStream input = channel.getInput();
        OutputStream output = channel.getOutput();
        String charset = getProperty( "encoding" );
        try
        {
            byte[] recvBuf = new byte[buffer_capacity];

            int length = read( input, recvBuf );

            byte[] errorBuf = "Unrecognized request".getBytes( charset );
            if ( length > 0 )
            {
                LOG.debug( "Received Data: " + new String( recvBuf, 0, length, charset ) );

                String code = getBusinessCode( recvBuf );

                OperationEntry entry = provideEntries.get( code );
                if ( entry == null )
                {
                    LOG.debug( "Unrecognized request, trancode is: " + code );
                    write( output, errorBuf, errorBuf.length );
                    return;
                }
                if ( entry.getSchema() == null )
                {
                    errorBuf = "Schema not found".getBytes( charset );
                    write( output, errorBuf, errorBuf.length );
                    return;
                }

                Element requestEl = raw2xml( recvBuf, length, entry.getSchema() );

                String serial = getSerialNo( requestEl, getProperty( "serial-path" ) );

                if ( entry.getData() == null )
                {
                    errorBuf = "Reponse Data not found".getBytes( charset );
                    write( output, errorBuf, errorBuf.length );
                    return;
                }
                Element rootEl = entry.getData().getRootElement();
                String index = getProperty( "selector" );
                if ( index.equals( "random()" ) )
                {
                    Random rand = new Random();
                    index = String.valueOf( rand.nextInt( rootEl.getChildren().size() ) + 1 );
                    LOG.info( "random index: " + index );
                }
                Element responseEl = (Element) XPath.selectSingleNode( rootEl, "response[" + index + "]" );

                byte[] sendBuf = new byte[buffer_capacity];
                int len = xml2raw( responseEl, entry.getSchema(), sendBuf );

                LOG.debug( "Send Data: " + new String( sendBuf, 0, len, charset ) );

                write( output, sendBuf, len );
            }

        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            try
            {
                String error = e.getMessage();
                if ( error == null )
                {
                    error = e.toString();
                }
                byte[] errorBuf = error.getBytes( charset );
                write( output, errorBuf, errorBuf.length );
            }
            catch ( IOException ex )
            {
                LOG.error( "写响应失败：" + e.getMessage() );
            }
        }
        finally
        {
            try
            {
                channel.close();
            }
            catch ( IOException e )
            {
                LOG.error( "通道关闭失败：" + e.getMessage() );
            }
        }
    }

    protected String getSerialNo( Element requestEl, String xpath )
        throws Exception
    {
        Element serialEl = (Element) XPath.selectSingleNode( requestEl, xpath );
        return ( ( serialEl != null ) ? serialEl.getText() : null );
    }

    @Override
    protected int xml2raw( Element dataEl, XmlSchema schema, byte[] result )
        throws Exception
    {
        dataEl.setName( "response" );
        return super.xml2raw( dataEl, schema, result );
    }

    protected Element raw2xml( byte[] buffer, int length, XmlSchema schema )
        throws Exception
    {
        return super.raw2xml( buffer, length, schema, "request" );
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
    protected int read( InputStream input, byte[] bytes )
        throws IOException
    {
        int count = 0;
        int p;
        int l;
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
                        for ( p = 0; ( l = input.read( bytes, p, count - p ) ) < count - p; )
                        {
                            p += l;
                        }
                    }
                    else
                    {
                        p = 0;
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

    protected String getBusinessCode( byte[] bytes )
    {
        int start = Integer.parseInt( getProperty( "code_begin" ) );
        int end = Integer.parseInt( getProperty( "code_end" ) );
        return new String( bytes, start, end - start );
    }

    @Override
    protected void dispose()
    {
        provideEntries.clear();
        transport = null;
        super.dispose();
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
    public void start()
        throws Exception
    {
        transport = new ServerSocket();
        transport.bind( socketAddress );
        poolExecutor.prestartCoreThread();
        acceptThread = new Thread( this );
        acceptThread.start();
        super.start();
    }

    @Override
    public void stop()
        throws Exception
    {
        if ( acceptThread != null )
        {
            try
            {
                acceptThread.interrupt();
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
            }
            finally
            {
                transport.close();
            }
        }
        super.stop();
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
            accepted( chnn );
        }
    }
}