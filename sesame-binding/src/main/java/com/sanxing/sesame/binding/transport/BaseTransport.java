package com.sanxing.sesame.binding.transport;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jbi.messaging.MessagingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.binding.Binding;
import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.Carrier;
import com.sanxing.sesame.binding.RecognizeException;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.logging.ErrorRecord;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.PerfRecord;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.util.W3CUtil;

public abstract class BaseTransport
    implements Transport
{
    public static final String SEND_TIME = "sendTime";

    private final Map<String, List<Carrier>> carriers = new Hashtable();

    private URI uri;

    private boolean keepAlive = false;

    @Override
    public void init( Element config )
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
                    properties.put( prop.getNodeName(), W3CUtil.getElementText( prop ) );
                }
                setProperties( properties );
            }
            catch ( XPathExpressionException e )
            {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        }
    }

    @Override
    public void addCarrier( String contextPath, Carrier receiver )
    {
        List list = carriers.get( contextPath );

        if ( list == null )
        {
            carriers.put( contextPath, list = new ArrayList() );
        }

        list.add( receiver );
    }

    @Override
    public void removeCarrier( String contextPath, Carrier carrier )
    {
        List list = carriers.get( contextPath );

        if ( list != null )
        {
            list.remove( carrier );
        }
    }

    protected Carrier[] getCarriers( String contextPath )
    {
        if ( contextPath == null )
        {
            return new Carrier[0];
        }
        List list = carriers.get( contextPath );
        if ( list != null )
        {
            return ( (Carrier[]) list.toArray( new Carrier[list.size()] ) );
        }

        return new Carrier[0];
    }

    protected Carrier getCarrier( MessageContext message )
    {
        Carrier carrier = (Carrier) message.getProperty( "carrier" );
        return carrier;
    }

    @Override
    public void setConfig( String contextPath, Element config )
        throws IllegalArgumentException
    {
    }

    @Override
    public void setURI( URI uri )
    {
        this.uri = uri;
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public boolean getKeepAlive()
    {
        return keepAlive;
    }

    @Override
    public void setKeepAlive( boolean on )
    {
        keepAlive = on;
    }

    protected void doPost( String contextPath, InputStream input, OutputStream output )
        throws MessagingException, BindingException
    {
        BinarySource source = new BinarySource( input );
        source.setEncoding( getCharacterEncoding() );
        BinaryResult result = new BinaryResult( output );
        result.setEncoding( getCharacterEncoding() );
        MessageContext message = new MessageContext( (Acceptor) this, source );
        message.setResult( result );
        message.setPath( contextPath );
        message.setMode( MessageContext.Mode.NON_BLOCK );

        postMessage( message );
    }

    protected void postMessage( MessageContext message )
        throws MessagingException, BindingException
    {
        Log log = LogFactory.getLog( "sesame.binding" );
        Log sensor = LogFactory.getLog( "sesame.system.sensor.exchange" );
        try
        {
            Carrier[] Carriers = getCarriers( message.getPath() );
            for ( Carrier carrier : Carriers )
            {
                long timeMillis = System.currentTimeMillis();
                message.setProperty( "sendTime", Long.valueOf( timeMillis ) );
                boolean result = carrier.post( message );

                if ( ( message.getMode() == MessageContext.Mode.BLOCK ) && ( sensor.isInfoEnabled() ) )
                {
                    PerfRecord perf = new PerfRecord();
                    perf.setSerial( message.getSerial().longValue() );
                    perf.setElapsedTime( System.currentTimeMillis() - timeMillis );
                    sensor.info( "-----------------------------------------------------------------exchange time--",
                        perf );
                }

                if ( result )
                {
                    return;
                }
            }

            Exception ex = new RecognizeException( String.format( "Could not recognize action(tx-code:[%s])", message.getAction() ) );
            if ( message.getBinding() != null )
            {
                message.setException( ex );
                Binding binding = message.getBinding();
                binding.handle( null, message );
                return;
            }

            throw ex;
        }
        catch ( BindingException e )
        {
            log.error( e.getMessage(), new ErrorRecord( message.getSerial().longValue(), e ) );
            Binding binding = message.getBinding();
            message.setException( e );
            binding.handle( null, message );
        }
        catch ( MessagingException e )
        {
            log.error( e.getMessage(), new ErrorRecord( message.getSerial().longValue(), e ) );
            Binding binding = message.getBinding();
            message.setException( e );
            binding.handle( null, message );
        }
        catch ( Throwable t )
        {
            Exception e = new RuntimeException( t.getMessage(), t );
            log.error( e.getMessage(), new ErrorRecord( message.getSerial().longValue(), e ) );
            message.setException( e );
            Binding binding = message.getBinding();
            if ( binding != null )
            {
                binding.handle( null, message );
                return;
            }

            Carrier carrier = getCarrier( message );
            carrier.post( message );
        }
    }

    protected byte[] doSend( String contextPath, byte[] input, int length )
        throws MessagingException, BindingException
    {
        BinarySource source = new BinarySource();
        source.setBytes( input, length );
        source.setEncoding( getCharacterEncoding() );
        source.setProperty( "contextRoot", getURI() );
        BinaryResult result = new BinaryResult();
        result.setEncoding( getCharacterEncoding() );
        MessageContext message = new MessageContext( (Acceptor) this, source );
        message.setResult( result );
        message.setPath( contextPath );
        message.setMode( MessageContext.Mode.BLOCK );

        postMessage( message );

        return result.getBytes();
    }

    protected void sendFault( MessageContext message )
        throws MessagingException, BindingException
    {
        if ( message.isAccepted() )
        {
            return;
        }
        message.setStatus( MessageContext.Status.FAULT );
        Binding binding = message.getBinding();
        binding.handle( message.getSource(), message );
        OperationContext operation = binding.getOperationContext( message.getAction() );
        if ( operation == null )
        {
            throw new BindingException( "Could not find operation for MessageContext: " + message );
        }

        Carrier carrier = getCarrier( message );
        carrier.post( message );
    }

    protected abstract void setProperties( Map<?, ?> paramMap )
        throws IllegalArgumentException;
}