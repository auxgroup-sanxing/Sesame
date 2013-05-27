package com.sanxing.sesame.binding.soap;

import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.component.BindingComponent;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.service.ServiceUnit;
import com.sanxing.sesame.util.JdomUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.w3c.dom.DocumentFragment;

public class SOAPClient
    extends BindingComponent
    implements ServiceUnitManager
{
    private static Logger LOG = LoggerFactory.getLogger( SOAPClient.class );

    private AtomicBoolean initialized = new AtomicBoolean( false );

    private AtomicBoolean started = new AtomicBoolean( false );

    private Map<QName, SOAPSender> senderMap = new HashMap();

    protected void init()
        throws JBIException
    {
        if ( this.initialized.compareAndSet( false, true ) )
            super.init();
    }

    public void start()
        throws JBIException
    {
        if ( this.started.compareAndSet( false, true ) )
            super.start();
    }

    public void stop()
        throws JBIException
    {
        if ( this.started.compareAndSet( true, false ) )
            super.stop();
    }

    public void shutDown()
        throws JBIException
    {
        if ( this.initialized.compareAndSet( true, false ) )
        {
            stop();
            super.shutDown();
        }
    }

    public void start( String serviceUnitName )
        throws DeploymentException
    {
        ServiceUnit unit = getServiceUnit( serviceUnitName );
        SOAPSender sender = new SOAPSender();
        try
        {
            sender.setconfig( unit );
            this.senderMap.put( unit.getServiceName(), sender );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw taskFailure( "start " + unit, e.getMessage() != null ? e.getMessage() : e.toString() );
        }
        super.start( serviceUnitName );
    }

    public void stop( String serviceUnitName )
        throws DeploymentException
    {
        super.stop( serviceUnitName );
    }

    protected void checkInitialized()
        throws JBIException
    {
        if ( !this.initialized.get() )
            throw new JBIException( "The Component is not initialized - please call init(...)" );
    }

    public void onMessageExchange( MessageExchange exchange )
        throws MessagingException
    {
        try
        {
            NormalizedMessage msg = exchange.getMessage( ExchangeConst.IN );
            Source response = null;
            SOAPSender sender = (SOAPSender) this.senderMap.get( exchange.getService() );
            response = sender.sendRequest( msg.getContent(), exchange );
            Document responseXML = JdomUtil.source2JDOMDocument( response );

            NormalizedMessage recmsg = exchange.createMessage();
            recmsg.setContent( JdomUtil.JDOMDocument2DOMSource( responseXML ) );
            exchange.setMessage( recmsg, ExchangeConst.OUT );
        }
        catch ( SoapFaultException e )
        {
            SOAPFault soapFault = e.getFault();
            Fault fault = exchange.createFault();
            fault.setContent( new DOMSource( soapFault ) );
            exchange.setFault( fault );
            exchange.setStatus( ExchangeStatus.ERROR );
        }
        catch ( Exception e )
        {
            Fault fault = exchange.createFault();
            Element faultEle = new Element( ExchangeConst.FAULT );
            faultEle.addContent( e.getMessage() );
            fault.setContent( JdomUtil.JDOMElement2DOMSource( faultEle ) );
            exchange.setFault( fault );
            exchange.setStatus( ExchangeStatus.ERROR );
        }

        send( exchange );
    }

    public ServiceEndpoint resolveEndpointReference( DocumentFragment epr )
    {
        return null;
    }

    public String deploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return ManagementSupport.createSuccessMessage( "deploy-" + serviceUnitName, "部署成功" );
    }

    public String undeploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return ManagementSupport.createSuccessMessage( "undeploy-" + serviceUnitName, "撤消部署成功" );
    }

    protected ServiceUnitManager createServiceUnitManager()
    {
        return this;
    }

    public void init( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
    }

    public void shutDown( String serviceUnitName )
        throws DeploymentException
    {
    }
}