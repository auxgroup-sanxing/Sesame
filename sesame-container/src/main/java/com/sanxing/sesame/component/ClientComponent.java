package com.sanxing.sesame.component;

import java.io.StringWriter;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

public class ClientComponent
    extends ComponentSupport
{
    static Logger log = LoggerFactory.getLogger( ClientComponent.class );

    private static ClientComponent _instance;

    public static synchronized ClientComponent getInstance()
    {
        if ( _instance == null )
        {
            _instance = new ClientComponent();
        }
        return _instance;
    }

    @Override
    protected ServiceUnitManager createServiceUnitManager()
    {
        return null;
    }

    public Source send( Source input, QName serviceName, QName interfaceName, QName operation, Long serial )
    {
        try
        {
            MessageExchange exchange = getExchangeFactory().createInOutExchange();
            exchange.setProperty( "sesame.exchange.platform.serial", serial );
            exchange.setService( serviceName );
            exchange.setInterfaceName( interfaceName );
            exchange.setOperation( operation );
            NormalizedMessage msg = exchange.createMessage();
            msg.setContent( input );
            exchange.setMessage( msg, "in" );
            sendSync( exchange );
            if ( exchange.getError() != null )
            {
                throw new RuntimeException( exchange.getError() );
            }
            if ( exchange.getFault() != null )
            {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty( "indent", "yes" );
                StringWriter buffer = new StringWriter();
                transformer.transform( exchange.getFault().getContent(), new StreamResult( buffer ) );
                throw new RuntimeException( buffer.toString() );
            }
            return exchange.getMessage( "out" ).getContent();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public String getDescription()
    {
        return "客户端服务组件";
    }

    @Override
    public boolean isBindingComponent()
    {
        return true;
    }

    @Override
    public boolean isEngineComponent()
    {
        return false;
    }

    @Override
    public ServiceEndpoint resolveEndpointReference( DocumentFragment epr )
    {
        return null;
    }
}