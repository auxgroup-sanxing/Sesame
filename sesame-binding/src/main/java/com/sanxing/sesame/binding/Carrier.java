package com.sanxing.sesame.binding;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataHandler;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.binding.codec.XMLSource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.logging.ErrorRecord;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.PerfRecord;
import com.sanxing.sesame.service.OperationContext;

public class Carrier
{
    public static final String BINDING_SERVICE_NAME = "sesame.binding.service.name";

    public static final String BINDING_ENDPOINT_NAME = "sesame.binding.endpoint.name";

    public static final String BINDING_OPERATION_NAME = "sesame.binding.operation.name";

    protected static final String SEND_TIME = "sendTime";

    private static final Logger LOG = LoggerFactory.getLogger( Carrier.class );

    private final AdapterComponent component;

    private final Map<Object, Object> cache;

    public Carrier( AdapterComponent component )
    {
        this.component = component;
        cache = new ConcurrentHashMap();
    }

    public boolean post( MessageContext message )
        throws MessagingException, BindingException
    {
        if ( message.isAccepted() )
        {
            Transport transport = message.getTransport();
            Binding[] bindingArray = component.getBindings( transport, message.getPath() );

            LOG.debug( "Bindings for " + transport.hashCode() + message.getPath() + ": " + bindingArray.length );
            int j = bindingArray.length;
            int i = 0;
            while ( true )
            {
                Binding binding = bindingArray[i];
                message.setBinding( binding );
                Map headers = new HashMap();
                boolean parsed = binding.parse( message, headers );
                if ( parsed )
                {
                    OperationContext operation = binding.getOperationContext( message.getAction() );
                    if ( operation != null )
                    {
                        if ( message.getMode() == MessageContext.Mode.BLOCK )
                        {
                            long timeoutMillis = message.getTimeout();
                            Source content = sendRequest( operation, message, timeoutMillis );

                            if ( message.getStatus() == MessageContext.Status.OK )
                            {
                                binding.assemble( content, message );
                            }
                            else
                            {
                                LOG.error( "Messaging exception", new ErrorRecord( message.getSerial().longValue(),
                                    message.getException() ) );
                                binding.handle( content, message );
                            }

                            return true;
                        }

                        sendRequest( operation, message );
                        return true;
                    }
                }
                ++i;
                if ( i >= j )
                {
                    return false;
                }
            }
        }
        Binding binding = message.getBinding();
        if ( message.getStatus() == MessageContext.Status.OK )
        {
            Map headers = new HashMap();
            boolean parsed = binding.parse( message, headers );
            if ( !( parsed ) )
            {
                throw new BindingException( "Parse response error for MessageContext: " + message );
            }
        }
        String operationName = (String) message.getProperty( "sesame.binding.operation.name" );
        OperationContext operation = binding.getServiceUnit().getOperationContext( operationName );
        if ( operation == null )
        {
            throw new BindingException( "Could not find operation for MessageContext: " + message );
        }
        return sendResponse( operation, message );
    }

    private Source sendRequest( OperationContext operation, MessageContext message, long timeoutMillis )
        throws MessagingException
    {
        MessageExchange exchange = component.createExchange( operation );

        Set<String> names = message.getPropertyNames();
        for ( String name : names )
        {
            exchange.setProperty( name, message.getProperty( name ) );
        }
        exchange.setProperty( "sesame.exchange.platform.serial", message.getSerial() );
        exchange.setProperty( "sesame.exchange.tx.proxy", operation.getServiceUnit().getName() );
        exchange.setProperty( "sesame.exchange.tx.action", operation.getAction() );
        exchange.setProperty( "sesame.exchange.consumer", component.getContext().getComponentName() );

        NormalizedMessage normalizedIn = exchange.createMessage();
        if ( message.getSource() instanceof XMLSource )
        {
            XMLSource xmlSource = (XMLSource) message.getSource();
            for ( String name : xmlSource.getPropertyNames() )
            {
                normalizedIn.setProperty( name, xmlSource.getProperty( name ) );
            }
            normalizedIn.setContent( xmlSource.getContent() );
            for ( String name : xmlSource.getAttachmentNames() )
            {
                normalizedIn.addAttachment( name, new DataHandler( xmlSource.getAttachment( name ) ) );
            }
        }
        else
        {
            normalizedIn.setContent( message.getSource() );
        }
        exchange.setMessage( normalizedIn, "in" );

        boolean success = component.sendSync( exchange, timeoutMillis );
        if ( !( success ) )
        {
            throw new MessagingException( "000001|Send Message timeout" );
        }

        if ( message.getResult() == null )
        {
            BinaryResult result = new BinaryResult();
            result.setEncoding( message.getTransport().getCharacterEncoding() );
            message.setResult( result );
        }
        Iterator iterator;
        Object id;
        if ( exchange.getStatus() == ExchangeStatus.ERROR )
        {
            Fault fault = exchange.getFault();
            message.setStatus( MessageContext.Status.FAULT );
            message.setException( exchange.getError() );
            BinaryResult result;
            if ( ( fault != null ) && ( message.getResult() instanceof BinaryResult ) )
            {
                result = (BinaryResult) message.getResult();

                Set<String> proptertyNames = fault.getPropertyNames();
                for ( String name : proptertyNames )
                {
                    result.setProperty( name, fault.getProperty( name ) );
                }
                Set attachmentNames = fault.getAttachmentNames();
                iterator = attachmentNames.iterator();
                while ( true )
                {
                    id = iterator.next();
                    DataHandler data = fault.getAttachment( (String) id );
                    result.addAttachment( (String) id, data.getDataSource() );

                    if ( !( iterator.hasNext() ) )
                    {
                        return ( ( fault != null ) ? fault.getContent() : null );
                    }
                }
            }
        }
        NormalizedMessage nm = exchange.getMessage( "out" );
        if ( ( nm != null ) && ( message.getResult() instanceof BinaryResult ) )
        {
            BinaryResult result = (BinaryResult) message.getResult();

            Set proptertyNames = nm.getPropertyNames();
            for ( id = proptertyNames.iterator(); ( (Iterator) id ).hasNext(); )
            {
                String name = (String) ( (Iterator) id ).next();
                result.setProperty( name, nm.getProperty( name ) );
            }
            Set<String> attachmentNames = nm.getAttachmentNames();
            for ( String name : attachmentNames )
            {
                DataHandler data = nm.getAttachment( name );
                result.addAttachment( name, data.getDataSource() );
            }
        }
        return ( ( nm != null ) ? nm.getContent() : (Source) null );
    }

    private void sendRequest( OperationContext operation, MessageContext message )
        throws MessagingException
    {
        MessageExchange exchange = component.createExchange( operation );

        for ( String name : message.getPropertyNames() )
        {
            exchange.setProperty( name, message.getProperty( name ) );
        }
        exchange.setProperty( "sesame.exchange.platform.serial", message.getSerial() );
        exchange.setProperty( "sesame.exchange.tx.proxy", operation.getServiceUnit().getName() );
        exchange.setProperty( "sesame.exchange.tx.action", operation.getAction() );
        exchange.setProperty( "sesame.exchange.consumer", component.getContext().getComponentName() );
        message.setProperty( "sesame.exchange.consumer", component.getContext().getComponentName() );

        NormalizedMessage normalizedIn = exchange.createMessage();
        if ( message.getSource() instanceof XMLSource )
        {
            XMLSource xmlsource = (XMLSource) message.getSource();
            Set<String> headerNames = xmlsource.getPropertyNames();
            for ( String headerName : headerNames )
            {
                normalizedIn.setProperty( headerName, xmlsource.getProperty( headerName ) );
            }
            normalizedIn.setContent( xmlsource.getContent() );
        }
        else
        {
            normalizedIn.setContent( message.getSource() );
        }

        exchange.setMessage( normalizedIn, "in" );
        getCache().put( exchange.getExchangeId(), message );

        exchange.setProperty( "sendTime", Long.valueOf( System.currentTimeMillis() ) );
        component.send( exchange );
    }

    private boolean sendResponse( OperationContext operation, MessageContext message )
        throws MessagingException, BindingException
    {
        MessageExchange exchange = (MessageExchange) getCache().get( message );
        if ( exchange == null )
        {
            LOG.error( "MessageExchange not Found. Context is: " + message );
            throw new MessagingException( "MessageExchange not Found. Context is: " + message );
        }

        getCache().remove( message );

        for ( String name : message.getPropertyNames() )
        {
            exchange.setProperty( name, message.getProperty( name ) );
        }

        if ( message.getStatus() == MessageContext.Status.OK )
        {
            NormalizedMessage normalizedOut = exchange.createMessage();
            if ( message.getResult() instanceof XMLResult )
            {
                XMLResult xmlResult = (XMLResult) message.getResult();
                Set<String> headerNames = xmlResult.getPropertyNames();
                for ( String headerName : headerNames )
                {
                    normalizedOut.setProperty( headerName, xmlResult.getProperty( headerName ) );
                }
                normalizedOut.setContent( xmlResult.getContent() );
            }

            exchange.setMessage( normalizedOut, "out" );
        }
        else
        {
            Fault fault = exchange.createFault();
            if ( message.getResult() instanceof XMLResult )
            {
                XMLResult xmlResult = (XMLResult) message.getResult();
                Set<String> headerNames = xmlResult.getPropertyNames();
                for ( String headerName : headerNames )
                {
                    fault.setProperty( headerName, xmlResult.getProperty( headerName ) );
                }
                fault.setContent( xmlResult.getContent() );
            }
            else
            {
                LOG.debug( "fault: " + message.getResult() );
                fault.setContent( new StreamSource( message.getResult().getSystemId() ) );
            }
            exchange.setFault( fault );
            exchange.setStatus( ExchangeStatus.ERROR );
        }

        component.send( exchange );
        return true;
    }

    protected void dispatchMessage( MessageExchange exchange, Binding binding )
        throws MessagingException, BindingException
    {
        if ( exchange.getRole() == MessageExchange.Role.PROVIDER )
        {
            BinaryResult result = new BinaryResult();
            result.setEncoding( binding.getTransport().getCharacterEncoding() );
            MessageContext context = new MessageContext( (Connector) binding.getTransport(), result );
            try
            {
                long serial = ( (Long) exchange.getProperty( "sesame.exchange.platform.serial" ) ).longValue();
                context.setSerial( serial );
                context.setProperty( "sesame.binding.operation.name", exchange.getOperation().getLocalPart() );
            }
            catch ( IllegalAccessException e )
            {
                throw new BindingException( e.getMessage(), e );
            }
            getCache().put( context, exchange );

            Set<String> names = exchange.getPropertyNames();
            for ( String name : names )
            {
                context.setProperty( name, exchange.getProperty( name ) );
            }

            NormalizedMessage nm = exchange.getMessage( "in" );
            Set<String> propertyNames = nm.getPropertyNames();
            for ( String name : propertyNames )
            {
                result.setProperty( name, nm.getProperty( name ) );
            }
            Set<String> attachmentName = nm.getAttachmentNames();
            for ( String name : attachmentName )
            {
                result.addAttachment( name, nm.getAttachment( name ).getDataSource() );
            }
            try
            {
                context.setBinding( binding );
                context.setProperty( "binding", binding );
                context.setProperty( "carrier", this );

                binding.assemble( nm.getContent(), context );
                Connector connector = (Connector) context.getTransport();
                connector.sendOut( context );
            }
            catch ( IOException e )
            {
                throw new BindingException( e.getMessage(), e );
            }

            return;
        }

        MessageContext context = (MessageContext) getCache().get( exchange.getExchangeId() );
        if ( context == null )
        {
            LOG.error( "MessageContext not Found. MessageExchange is: " + exchange.getExchangeId() );
            throw new BindingException( "MessageContext not Found. MessageExchange is: " + exchange.getExchangeId() );
        }

        getCache().remove( exchange.getExchangeId() );

        long timeMillis = ( (Long) exchange.getProperty( "sendTime" ) ).longValue();
        Log sensor = LogFactory.getLog( "sesame.system.sensor.exchange" );
        PerfRecord perf = new PerfRecord();
        perf.setElapsedTime( System.currentTimeMillis() - timeMillis );
        perf.setSerial( context.getSerial().longValue() );
        sensor.info( "-----------------------------------------------------------------exchange time--", perf );
        try
        {
            Iterator iter;
            Object id;
            if ( exchange.getStatus() == ExchangeStatus.ERROR )
            {
                Fault fault = exchange.getFault();
                context.setStatus( MessageContext.Status.FAULT );
                context.setException( exchange.getError() );

                if ( ( fault != null ) && ( context.getResult() instanceof BinaryResult ) )
                {
                    BinaryResult result = (BinaryResult) context.getResult();

                    Set<String> proptertyNames = fault.getPropertyNames();
                    for ( String name : proptertyNames )
                    {
                        result.setProperty( name, fault.getProperty( name ) );
                    }
                    Set attachmentNames = fault.getAttachmentNames();
                    for ( iter = attachmentNames.iterator(); iter.hasNext(); )
                    {
                        id = iter.next();
                        DataHandler data = fault.getAttachment( (String) id );
                        result.addAttachment( (String) id, data.getDataSource() );
                    }
                }
                LOG.debug( "Handling fault", context.getException() );
                binding.handle( ( fault != null ) ? fault.getContent() : null, context );
            }
            else
            {
                NormalizedMessage nm = exchange.getMessage( "out" );
                if ( ( nm != null ) && ( context.getResult() instanceof BinaryResult ) )
                {
                    BinaryResult result = (BinaryResult) context.getResult();

                    Set proptertyNames = nm.getPropertyNames();
                    for ( id = proptertyNames.iterator(); ( (Iterator) id ).hasNext(); )
                    {
                        String name = (String) ( (Iterator) id ).next();
                        result.setProperty( name, nm.getProperty( name ) );
                    }
                    Set<String> attachmentNames = nm.getAttachmentNames();
                    for ( String name : attachmentNames )
                    {
                        DataHandler data = nm.getAttachment( name );
                        result.addAttachment( name, data.getDataSource() );
                    }
                }

                binding.assemble( ( nm != null ) ? nm.getContent() : null, context );
            }

            Acceptor acceptor = (Acceptor) context.getTransport();
            acceptor.reply( context );
        }
        catch ( Exception e )
        {
            throw new MessagingException( e.getMessage(), e );
        }
    }

    private Map<Object, Object> getCache()
    {
        return cache;
    }
}