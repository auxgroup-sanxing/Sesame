package com.sanxing.sesame.wtc.transport;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.core.naming.JNDIUtil;
import com.sanxing.sesame.wtc.ByteUtil;
import com.sanxing.sesame.wtc.WTCEncoder;
import com.sanxing.sesame.wtc.output.SOPOutputter;
import com.sanxing.sesame.wtc.proxy.WTCHandler;
import com.sanxing.sesame.wtc.proxy.WTCProxy;
import com.sanxing.sesame.wtc.proxy.WTCRequest;
import com.sanxing.sesame.wtc.proxy.WTCResponse;
import com.sanxing.sesame.wtc.sop.SOPRequest;
import com.sanxing.sesame.wtc.sop.SOPResponse;
import com.sanxing.sesame.wtc.sop.SOPSysHead;
import com.sanxing.sesame.wtc.sop.SOPTranHead;
import com.sanxing.sesame.wtc.sop.SOPUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.jbi.messaging.MessagingException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WTCAcceptor
    extends BaseTransport
    implements Acceptor, WTCHandler
{
    private static final Logger LOG = LoggerFactory.getLogger( WTCAcceptor.class );

    private boolean active = false;

    private Map<?, ?> properties = new HashMap();

    private WTCProxy proxy;

    private InitialContext context;

    private String jndiName;

    private long timeout = 100000L;

    public void open()
        throws IOException
    {
        this.active = true;
        this.proxy = new WTCProxy();
        this.context = JNDIUtil.getInitialContext();

        this.jndiName = getURI().getAuthority();
        try
        {
            this.context.bind( this.jndiName, this.proxy );
            LOG.debug( "WTC:JNDI bind " + this.proxy.hashCode() + " success." );
            this.proxy.register( this );
        }
        catch ( NamingException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    public void close()
        throws IOException
    {
        this.active = false;
        try
        {
            this.context.unbind( this.jndiName );
            LOG.debug( "WTC:JNDI unbind " + this.proxy.hashCode() + " success." );
        }
        catch ( NamingException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    public boolean isActive()
    {
        return this.active;
    }

    public String getCharacterEncoding()
    {
        return "GBK";
    }

    public void reply( MessageContext context )
        throws BindingException, IOException
    {
    }

    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
        this.properties = properties;
    }

    public WTCProxy getProxy()
    {
        return this.proxy;
    }

    protected BinaryResult doSend( String contextPath, BinarySource source )
        throws MessagingException, BindingException
    {
        BinaryResult result = new BinaryResult();
        result.setEncoding( getCharacterEncoding() );
        MessageContext message = new MessageContext( this, source );
        message.setResult( result );
        message.setPath( contextPath );
        message.setMode( MessageContext.Mode.BLOCK );
        message.setTimeout( this.timeout );
        postMessage( message );
        return result;
    }

    public WTCResponse handle( WTCRequest request )
    {
        WTCResponse response = new WTCResponse();
        byte[] inputBuf = request.getContent();
        if ( LOG.isInfoEnabled() )
        {
            LOG.info( "WTC:receive message from WTC: " );
            LOG.info( SOPOutputter.format( inputBuf ) );
        }

        SOPRequest req = new SOPRequest();
        try
        {
            req = parseRequest( inputBuf );
            if ( LOG.isInfoEnabled() )
            {
                LOG.info( "SESAME平台接收请求报文:" );
                LOG.info( req.toString() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "报文解析失败: " + e.getMessage() );
            return handleFault( "9999990", "报文解析失败" );
        }

        try
        {
            verify( req );
        }
        catch ( Exception e )
        {
            LOG.error( "MAC 校验失败", e );
            return handleFault( "9999991", "MAC 校验失败" );
        }

        BinaryResult result = new BinaryResult();
        try
        {
            result = sendRequest( req );

            byte[] rtnBytes = response( req, result );
            response.setContent( rtnBytes );
            if ( LOG.isInfoEnabled() )
            {
                LOG.info( "WTC:send message to WTC: " );
                LOG.info( SOPOutputter.format( rtnBytes ) );
                SOPResponse res = new SOPResponse();
                res.decode( rtnBytes );
                LOG.info( "SESAME平台发送响应报文:" );
                LOG.info( res.toString() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "内部处理失败: " + e.getMessage() );
            return handleFault( "9999999", "内部处理失败" );
        }

        return response;
    }

    private byte[] response( SOPRequest req, BinaryResult result )
    {
        SOPSysHead reqHead = req.getHead();
        SOPTranHead tranHead = req.getTranHead();
        String tranCode = tranHead.getTranCode();

        byte[] offsetBytes = (byte[]) result.getProperty( WTCEncoder.OFFSET );
        if ( offsetBytes == null )
        {
            offsetBytes = new byte[20];
        }
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "passwd offset: " + SOPOutputter.format( offsetBytes ) );
        }

        String pinFlag = reqHead.getPinFlag();
        String pinSeed = reqHead.getPinSeed();
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "pinFlag: " + pinFlag + "; pinSeed: " + pinSeed );
        }

        SOPSysHead head = new SOPSysHead();
        head.init( tranCode );
        head.setPasswdOffset( offsetBytes );
        head.setPinSeed( pinSeed );
        head.setPinFlag( pinFlag );
        byte[] body = result.getBytes();
        byte[] rtnHead = head.encode( body );
        byte[] rtnBytes = ByteUtil.cancat( rtnHead, body );
        return rtnBytes;
    }

    private BinaryResult sendRequest( SOPRequest req )
        throws MessagingException, BindingException
    {
        byte[] inputBuf = req.getContent();
        SOPTranHead tranHead = req.getTranHead();
        String tranCode = tranHead.getTranCode();

        BinarySource input = new BinarySource();
        input.setEncoding( getCharacterEncoding() );
        input.setBytes( inputBuf, inputBuf.length );
        input.setProperty( "tranCode", tranCode );
        input.setProperty( "contextRoot", getURI() );
        String contextPath = "/";
        BinaryResult result = new BinaryResult();
        result = doSend( contextPath, input );
        return result;
    }

    private void verify( SOPRequest request )
    {
        byte[] inputBuf = request.getContent();

        if ( !SOPUtil.verifyMAC( inputBuf ) )
            throw new RuntimeException( "MAC 校验失败." );
    }

    private SOPRequest parseRequest( byte[] inputBuf )
    {
        SOPRequest request = new SOPRequest();
        request.decode( inputBuf );
        return request;
    }

    public WTCResponse handleFault( String errCode, String errMsg )
    {
        SOPResponse resp = new SOPResponse( errCode, errMsg );
        byte[] content = resp.encode();
        if ( LOG.isInfoEnabled() )
        {
            LOG.info( "WTC:send fault message to WTC: " );
            LOG.info( SOPOutputter.format( content ) );
        }

        WTCResponse response = new WTCResponse();
        response.setContent( content );
        return response;
    }
}
