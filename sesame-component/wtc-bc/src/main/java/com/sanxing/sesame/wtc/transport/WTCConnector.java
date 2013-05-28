package com.sanxing.sesame.wtc.transport;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.wtc.ByteUtil;
import com.sanxing.sesame.wtc.WTCEncoder;
import com.sanxing.sesame.wtc.output.SOPOutputter;
import com.sanxing.sesame.wtc.sop.SOPRequest;
import com.sanxing.sesame.wtc.sop.SOPResponse;
import com.sanxing.sesame.wtc.sop.SOPSysHead;
import com.sanxing.sesame.wtc.sop.SOPTranHead;
import com.sanxing.sesame.wtc.sop.SOPUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weblogic.wtc.gwt.TuxedoConnection;
import weblogic.wtc.gwt.TuxedoConnectionFactory;
import weblogic.wtc.jatmi.Reply;
import weblogic.wtc.jatmi.TypedCArray;

public class WTCConnector
    extends BaseTransport
    implements Connector
{
    private static final Logger LOG = LoggerFactory.getLogger( WTCConnector.class );

    private Map<?, ?> properties;

    private boolean active;

    private String tuxedoService;

    public WTCConnector()
    {
        Map properties = new HashMap();
        properties.put( "encoding", "GBK" );

        this.properties = properties;
    }

    public void open()
        throws IOException
    {
        this.active = true;

        this.tuxedoService = getURI().getAuthority();
    }

    public void close()
        throws IOException
    {
        this.active = false;
    }

    public boolean isActive()
    {
        return this.active;
    }

    public String getProperty( String key )
    {
        return (String) this.properties.get( key );
    }

    public String getCharacterEncoding()
    {
        return getProperty( "encoding" );
    }

    private SOPRequest parseRequest( byte[] inputBuf )
    {
        SOPRequest request = new SOPRequest();
        request.decodeNoSyshead( inputBuf );
        return request;
    }

    private byte[] reform( SOPRequest request, BinarySource source )
    {
        SOPTranHead tranHead = request.getTranHead();
        String tranCode = tranHead.getTranCode();
        byte[] inputBuf = request.getContent();

        byte[] offset = (byte[]) source.getProperty( WTCEncoder.OFFSET );
        if ( offset == null )
        {
            offset = new byte[20];
        }
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "passwd offset: " + SOPOutputter.format( offset ) );
        }

        String channelFrom = (String) source.getProperty( "channel_from" );
        String channelTo = (String) source.getProperty( "channel_to" );
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "channelFrom: " + channelFrom + "; channelTo: " + channelTo );
        }

        String pinseed = (String) source.getProperty( "pin_seed" );
        String pinflag = (String) source.getProperty( "pin_flag" );
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "pinseed: " + pinseed + "; pinflag: " + pinflag );
        }

        SOPSysHead head = new SOPSysHead();
        head.init( tranCode );
        head.setPasswdOffset( offset );
        if ( pinseed != null )
        {
            head.setPinSeed( pinseed );
        }
        if ( pinflag != null )
        {
            head.setPinFlag( pinflag );
        }
        if ( channelFrom != null )
        {
            head.setChannelFrom( channelFrom );
        }
        if ( channelTo != null )
        {
            head.setChannelTo( channelTo );
        }
        byte[] sysHead = head.encode( inputBuf );
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "WTC:construct sop system header: " );
            LOG.debug( SOPOutputter.format( sysHead ) );
        }

        byte[] sendBytes = ByteUtil.cancat( sysHead, inputBuf );
        if ( LOG.isInfoEnabled() )
        {
            LOG.info( "WTC:send request to service [" + this.tuxedoService + "]: " );
            LOG.info( SOPOutputter.format( sendBytes ) );
            SOPRequest req = new SOPRequest();
            req.decode( sendBytes );
            LOG.info( "SESAME平台发送请求报文:" );
            LOG.info( req.toString() );
        }
        return sendBytes;
    }

    public byte[] sendRequest( byte[] request )
        throws BindingException
    {
        TypedCArray sendCArray = buildCArray( request );
        try
        {
            Context ctx = new InitialContext();
            TuxedoConnectionFactory tuxedoFactory =
                (TuxedoConnectionFactory) ctx.lookup( "tuxedo.services.TuxedoConnection" );
            TuxedoConnection tuxedoConn = tuxedoFactory.getTuxedoConnection();
            Reply reply = tuxedoConn.tpcall( this.tuxedoService, sendCArray, 0 );
            TypedCArray backCArray = (TypedCArray) reply.getReplyBuffer();
            tuxedoConn.tpterm();
            byte[] recvBytes = backCArray.carray;
            if ( LOG.isInfoEnabled() )
            {
                LOG.info( "WTC:receive response from service [" + this.tuxedoService + "]: " );
                LOG.info( SOPOutputter.format( recvBytes ) );
                SOPResponse res = new SOPResponse();
                res.decode( recvBytes );
                LOG.info( "SESAME平台接收响应报文:" );
                LOG.info( res.toString() );
            }
            return recvBytes;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new BindingException( e.getMessage() );
        }
    }

    private void handleResponse( MessageContext context, String tranCode, byte[] recvBytes )
        throws Exception
    {
        BinaryResult result = new BinaryResult();
        result.setEncoding( getProperty( "encoding" ) );
        result.write( recvBytes );

        byte[] errBytes = new byte[7];
        System.arraycopy( recvBytes, 141, errBytes, 0, 7 );
        String errcode = new String( errBytes );
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "WTC:errcode=[" + errcode + "]" );
        }
        if ( errcode.equalsIgnoreCase( "AAAAAAA" ) )
        {
            LOG.debug( "WTC:Transaction success, send response." );
            context.setResult( result );
            postMessage( context );
        }
        else
        {
            LOG.debug( "WTC:Transaction fail, the fault name: fault" + tranCode );
            result.setProperty( "fault-name", "fault" + tranCode );
            context.setResult( result );
            sendFault( context );
        }
    }

    public void sendOut( MessageContext context )
        throws BindingException, IOException
    {
        BinarySource source = (BinarySource) context.getSource();

        byte[] inputBuf = source.getBytes();
        if ( LOG.isInfoEnabled() )
        {
            LOG.info( "WTC:receive from component: " );
            LOG.info( SOPOutputter.format( inputBuf ) );
        }

        SOPRequest request = new SOPRequest();
        try
        {
            request = parseRequest( inputBuf );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "组件编码请求报文:" );
                LOG.debug( request.toString() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "报文解析失败: " + e.getMessage() );
            throw new BindingException( e.getMessage() );
        }

        try
        {
            String tranCode = request.getTranHead().getTranCode();

            byte[] sendBytes = reform( request, source );

            byte[] recvBytes = sendRequest( sendBytes );

            if ( !SOPUtil.verifyMAC( recvBytes ) )
            {
                throw new BindingException( "MAC 校验失败." );
            }

            handleResponse( context, tranCode, recvBytes );
        }
        catch ( Exception e )
        {
            LOG.error( "报文处理失败: " + e.getMessage() );
            throw new BindingException( e.getMessage() );
        }
    }

    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
    }

    private TypedCArray buildCArray( byte[] input )
    {
        int length = input.length;
        TypedCArray output = new TypedCArray( length );
        for ( int i = 0; i < length; i++ )
        {
            output.carray[i] = input[i];
        }
        return output;
    }
}
