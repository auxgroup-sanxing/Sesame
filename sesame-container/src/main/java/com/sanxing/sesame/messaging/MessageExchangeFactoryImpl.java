package com.sanxing.sesame.messaging;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.RobustInOnly;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import com.sanxing.sesame.mbean.ComponentContextImpl;
import com.sanxing.sesame.uuid.IdGenerator;

public class MessageExchangeFactoryImpl
    implements MessageExchangeFactory
{
    private QName interfaceName;

    private QName serviceName;

    private QName operationName;

    private ServiceEndpoint endpoint;

    private final IdGenerator idGenerator;

    private ComponentContextImpl context;

    private final AtomicBoolean closed;

    public MessageExchangeFactoryImpl( IdGenerator idGen, AtomicBoolean closed )
    {
        idGenerator = idGen;
        this.closed = closed;
    }

    protected void checkNotClosed()
        throws MessagingException
    {
        if ( closed.get() )
        {
            throw new MessagingException( "DeliveryChannel has been closed." );
        }
    }

    @Override
    public MessageExchange createExchange( URI pattern )
        throws MessagingException
    {
        checkNotClosed();
        MessageExchange result = null;
        if ( pattern != null )
        {
            if ( ( pattern.equals( MessageExchangeSupport.IN_ONLY ) )
                || ( pattern.equals( MessageExchangeSupport.WSDL2_IN_ONLY ) ) )
            {
                result = createInOnlyExchange();
            }
            else if ( ( pattern.equals( MessageExchangeSupport.IN_OUT ) )
                || ( pattern.equals( MessageExchangeSupport.WSDL2_IN_OUT ) ) )
            {
                result = createInOutExchange();
            }
            else if ( ( pattern.equals( MessageExchangeSupport.IN_OPTIONAL_OUT ) )
                || ( pattern.equals( MessageExchangeSupport.WSDL2_IN_OPTIONAL_OUT ) ) )
            {
                result = createInOptionalOutExchange();
            }
            else if ( ( pattern.equals( MessageExchangeSupport.ROBUST_IN_ONLY ) )
                || ( pattern.equals( MessageExchangeSupport.WSDL2_ROBUST_IN_ONLY ) ) )
            {
                result = createRobustInOnlyExchange();
            }
        }
        if ( result == null )
        {
            throw new MessagingException( "Do not understand pattern: " + pattern );
        }
        return result;
    }

    @Override
    public InOnly createInOnlyExchange()
        throws MessagingException
    {
        checkNotClosed();
        InOnlyImpl result = new InOnlyImpl( getExchangeId() );
        setDefaults( result );
        return result;
    }

    @Override
    public RobustInOnly createRobustInOnlyExchange()
        throws MessagingException
    {
        checkNotClosed();
        RobustInOnlyImpl result = new RobustInOnlyImpl( getExchangeId() );
        setDefaults( result );
        return result;
    }

    @Override
    public InOut createInOutExchange()
        throws MessagingException
    {
        checkNotClosed();
        InOutImpl result = new InOutImpl( getExchangeId() );
        setDefaults( result );
        return result;
    }

    @Override
    public InOptionalOut createInOptionalOutExchange()
        throws MessagingException
    {
        checkNotClosed();
        InOptionalOutImpl result = new InOptionalOutImpl( getExchangeId() );
        setDefaults( result );
        return result;
    }

    @Override
    public MessageExchange createExchange( QName svcName, QName opName )
        throws MessagingException
    {
        checkNotClosed();
        InOptionalOutImpl me = new InOptionalOutImpl( getExchangeId() );
        setDefaults( me );
        me.setService( svcName );
        me.setOperation( opName );
        return me;
    }

    protected String getExchangeId()
    {
        return idGenerator.generateId();
    }

    public ServiceEndpoint getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint( ServiceEndpoint endpoint )
    {
        this.endpoint = endpoint;
    }

    public QName getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName( QName interfaceName )
    {
        this.interfaceName = interfaceName;
    }

    public QName getServiceName()
    {
        return serviceName;
    }

    public void setServiceName( QName serviceName )
    {
        this.serviceName = serviceName;
    }

    public QName getOperationName()
    {
        return operationName;
    }

    public void setOperationName( QName operationName )
    {
        this.operationName = operationName;
    }

    public ComponentContextImpl getContext()
    {
        return context;
    }

    public void setContext( ComponentContextImpl context )
    {
        this.context = context;
    }

    protected void setDefaults( MessageExchangeImpl exchange )
    {
        exchange.setOperation( getOperationName() );
        if ( endpoint != null )
        {
            exchange.setEndpoint( getEndpoint() );
        }
        else
        {
            exchange.setService( serviceName );
            exchange.setInterfaceName( interfaceName );
        }

        if ( getContext() != null )
        {
            exchange.setSourceContext( getContext() );
            PojoMarshaler marshaler = getContext().getActivationSpec().getMarshaler();
            if ( marshaler != null )
            {
                exchange.setMarshaler( marshaler );
            }
        }
        exchange.setProperty( "com.sanxing.sesame.datestamp", new PrettyCalendar() );
    }

    public static class PrettyCalendar
        extends GregorianCalendar
    {
        @Override
        public String toString()
        {
            return new SimpleDateFormat().format( getTime() );
        }
    }
}