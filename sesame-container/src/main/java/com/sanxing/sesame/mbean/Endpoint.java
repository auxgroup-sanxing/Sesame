package com.sanxing.sesame.mbean;

import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.component.ClientComponent;
import com.sanxing.sesame.exception.FaultException;
import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.jaxp.StringSource;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import com.sanxing.sesame.servicedesc.ExternalEndpoint;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import com.sanxing.sesame.servicedesc.LinkedEndpoint;
import com.sanxing.sesame.util.QNameUtil;
import com.sanxing.sesame.util.W3CUtil;

public class Endpoint
    implements EndpointMBean, MBeanInfoProvider
{
    private static final Logger LOG = LoggerFactory.getLogger( Endpoint.class );

    private final AbstractEndpoint endpoint;

    private final Registry registry;

    public Endpoint( AbstractEndpoint endpoint, Registry registry )
    {
        this.endpoint = endpoint;
        this.registry = registry;
    }

    @Override
    public String getRemoteContainers()
    {
        String temp = "";
        InternalEndpoint ipoint = (InternalEndpoint) endpoint;
        for ( InternalEndpoint rpoint : ipoint.getRemoteEndpoints() )
        {
            temp = temp + rpoint.getComponentNameSpace().getContainerName();
        }
        return temp;
    }

    @Override
    public String getEndpointName()
    {
        return endpoint.getEndpointName();
    }

    @Override
    public QName[] getInterfaces()
    {
        return endpoint.getInterfaces();
    }

    @Override
    public QName getServiceName()
    {
        return endpoint.getServiceName();
    }

    @Override
    public String loadReference()
    {
        try
        {
            return W3CUtil.asIndentedXML( endpoint.getAsReference( null ) );
        }
        catch ( TransformerException e )
        {
        }
        return null;
    }

    @Override
    public String loadWSDL()
    {
        try
        {
            return W3CUtil.asXML( registry.getEndpointDescriptor( endpoint ) );
        }
        catch ( Exception e )
        {
        }
        return null;
    }

    @Override
    public String getComponentName()
    {
        if ( endpoint.getComponentNameSpace() != null )
        {
            return endpoint.getComponentNameSpace().getName();
        }
        return null;
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "endpointName", "name of the endpoint" );
        helper.addAttribute( getObjectToManage(), "serviceName", "name of the service" );
        helper.addAttribute( getObjectToManage(), "componentName", "component name of the service unit" );
        helper.addAttribute( getObjectToManage(), "interfaces", "interfaces implemented by this endpoint" );
        helper.addAttribute( getObjectToManage(), "remoteContainers", "containers of remote endpoints" );
        return helper.getAttributeInfos();
    }

    @Override
    public MBeanOperationInfo[] getOperationInfos()
        throws JMException
    {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation( getObjectToManage(), "loadReference", "retrieve the endpoint reference" );
        helper.addOperation( getObjectToManage(), "loadWSDL", "retrieve the wsdl description of this endpoint" );
        helper.addOperation( getObjectToManage(), "send", "send a simple message exchange to test this endpoint" );
        return helper.getOperationInfos();
    }

    @Override
    public Object getObjectToManage()
    {
        return this;
    }

    @Override
    public String getName()
    {
        return endpoint.getServiceName() + endpoint.getEndpointName();
    }

    @Override
    public String getType()
    {
        return "Endpoint";
    }

    @Override
    public String getSubType()
    {
        if ( endpoint instanceof InternalEndpoint )
        {
            return "Internal";
        }
        if ( endpoint instanceof LinkedEndpoint )
        {
            return "Linked";
        }
        if ( endpoint instanceof ExternalEndpoint )
        {
            return "External";
        }
        return null;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public void setPropertyChangeListener( PropertyChangeListener l )
    {
    }

    protected AbstractEndpoint getEndpoint()
    {
        return endpoint;
    }

    public String send( String content, String operation, String mep )
    {
        try
        {
            ClientComponent client = ClientComponent.getInstance();
            MessageExchange me = client.getExchangeFactory().createExchange( URI.create( mep ) );
            NormalizedMessage nm = me.createMessage();
            me.setMessage( nm, "in" );
            nm.setContent( new StringSource( content ) );
            me.setEndpoint( endpoint );
            if ( operation != null )
            {
                me.setOperation( QNameUtil.parse( operation ) );
            }
            client.sendSync( me );
            if ( me.getError() != null )
            {
                throw me.getError();
            }
            if ( me.getFault() != null )
            {
                throw FaultException.newInstance( me );
            }
            if ( me.getMessage( "out" ) != null )
            {
                return new SourceTransformer().contentToString( me.getMessage( "out" ) );
            }

            return null;
        }
        catch ( Exception e )
        {
            return null;
        }
    }
}