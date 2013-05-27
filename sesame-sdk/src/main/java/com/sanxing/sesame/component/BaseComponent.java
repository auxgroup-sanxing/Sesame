package com.sanxing.sesame.component;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.exception.FaultException;
import com.sanxing.sesame.exception.NotInitialisedYetException;
import com.sanxing.sesame.management.BaseLifeCycle;

public abstract class BaseComponent
    extends BaseLifeCycle
    implements ComponentLifeCycle
{
    protected Logger logger = LoggerFactory.getLogger( BaseComponent.class );

    private ComponentContext context;

    private ObjectName extensionMBeanName;

    private QName service;

    private String endpoint;

    private MessageExchangeFactory exchangeFactory;

    private String name = null;

    private String description = "POJO Component";

    private ServiceEndpoint serviceEndpoint;

    private DeliveryChannel channel;

    protected BaseComponent()
    {
    }

    protected BaseComponent( QName service, String endpoint )
    {
        this.service = service;
        this.endpoint = endpoint;
    }

    @Override
    public String getName()
    {
        return ( ( name == null ) ? super.getName() : name );
    }

    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    @Override
    public void init( ComponentContext cc )
        throws JBIException
    {
        context = cc;
        channel = context.getDeliveryChannel();
        init();
        if ( ( service != null ) && ( endpoint != null ) )
        {
            serviceEndpoint = context.activateEndpoint( service, endpoint );
        }
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        if ( serviceEndpoint != null )
        {
            context.deactivateEndpoint( serviceEndpoint );
        }
        exchangeFactory = null;
        super.shutDown();
    }

    @Override
    public ObjectName getExtensionMBeanName()
    {
        return extensionMBeanName;
    }

    public void setExtensionMBeanName( ObjectName extensionMBeanName )
    {
        this.extensionMBeanName = extensionMBeanName;
    }

    public ComponentContext getContext()
    {
        return context;
    }

    public QName getService()
    {
        return service;
    }

    public void setService( QName service )
    {
        this.service = service;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint( String endpoint )
    {
        this.endpoint = endpoint;
    }

    public MessageExchangeFactory getExchangeFactory()
        throws MessagingException
    {
        if ( ( exchangeFactory == null ) && ( context != null ) )
        {
            exchangeFactory = getDeliveryChannel().createExchangeFactory();
        }
        return exchangeFactory;
    }

    public DeliveryChannel getDeliveryChannel()
        throws MessagingException
    {
        if ( channel == null )
        {
            throw new NotInitialisedYetException();
        }
        return channel;
    }

    @Override
    protected void init()
        throws JBIException
    {
        super.init();
    }

    public void done( MessageExchange exchange )
        throws MessagingException
    {
        exchange.setStatus( ExchangeStatus.DONE );
        getDeliveryChannel().send( exchange );
    }

    public void send( MessageExchange exchange )
        throws MessagingException
    {
        getDeliveryChannel().send( exchange );
    }

    public boolean sendSync( MessageExchange exchange )
        throws MessagingException
    {
        return getDeliveryChannel().sendSync( exchange );
    }

    public boolean sendSync( MessageExchange exchange, long timeMillis )
        throws MessagingException
    {
        return getDeliveryChannel().sendSync( exchange, timeMillis );
    }

    public void answer( MessageExchange exchange, NormalizedMessage answer )
        throws MessagingException
    {
        exchange.setMessage( answer, ExchangeConst.OUT );
        getDeliveryChannel().send( exchange );
    }

    public void fail( MessageExchange exchange, Fault fault )
        throws MessagingException
    {
        if ( ( exchange instanceof InOnly ) || ( fault == null ) )
        {
            exchange.setError( new FaultException( "Fault occured for in-only exchange", exchange, fault ) );
        }
        else
        {
            exchange.setFault( fault );
        }
        getDeliveryChannel().send( exchange );
    }

    public void fail( MessageExchange exchange, Exception error )
        throws MessagingException
    {
        if ( ( exchange instanceof InOnly ) || ( !( error instanceof FaultException ) ) )
        {
            exchange.setError( error );
        }
        else
        {
            FaultException faultException = (FaultException) error;
            exchange.setFault( faultException.getFault() );
        }
        getDeliveryChannel().send( exchange );
    }

    protected boolean isInAndOut( MessageExchange exchange )
    {
        return ( ( exchange instanceof InOut ) || ( exchange instanceof InOptionalOut ) );
    }
}