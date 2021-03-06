package com.sanxing.sesame.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.wsdl.BindingOperation;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;

public class OperationContext
{
    private QName serviceName;

    private QName interfaceName;

    private QName operationName;

    private String endpointName;

    private ServiceUnit serviceUnit;

    private BindingOperation operation;

    private String action;

    private ReferenceEntry ref;

    private final Map<String, QName> elementMap = new HashMap();

    public OperationContext( BindingOperation operation )
    {
        this.operation = operation;
        setOperationName( new QName( operation.getName() ) );
        Iterator extIterator = operation.getExtensibilityElements().iterator();
        while ( extIterator.hasNext() )
        {
            Object ext = extIterator.next();
            if ( ext instanceof SOAPOperation )
            {
                SOAPOperation op = (SOAPOperation) ext;
                setAction( op.getSoapActionURI() );
                break;
            }
        }

        if ( operation.getOperation() != null )
        {
            cacheElements( operation.getOperation() );
        }
    }

    public OperationContext( Operation operation )
    {
        setOperationName( new QName( operation.getName() ) );
        setAction( null );
        cacheElements( operation );
    }

    private void cacheElements( Operation operation )
    {
        Input input = operation.getInput();
        if ( ( input != null ) && ( input.getMessage() != null ) )
        {
            Part part = input.getMessage().getPart( "parameters" );
            if ( part != null )
            {
                elementMap.put( "input", part.getElementName() );
            }
        }
        Output output = operation.getOutput();
        if ( ( output != null ) && ( output.getMessage() != null ) )
        {
            Part part = output.getMessage().getPart( "parameters" );
            if ( part != null )
            {
                elementMap.put( "output", part.getElementName() );
            }
        }
        Map faults = operation.getFaults();
        for ( Iterator iterator = faults.keySet().iterator(); iterator.hasNext(); )
        {
            String key = (String) iterator.next();
            Fault fault = (Fault) faults.get( key );
            if ( fault.getMessage() != null )
            {
                Part part = fault.getMessage().getPart( "parameters" );
                if ( part == null )
                {
                    continue;
                }
                elementMap.put( "fault:" + fault.getName(), part.getElementName() );
            }
        }
    }

    public QName getQName()
    {
        return new QName( serviceUnit.getName(), operationName.getLocalPart() );
    }

    public QName getServcieName()
    {
        return serviceName;
    }

    protected void setServcieName( QName serviceName )
    {
        this.serviceName = serviceName;
    }

    public QName getInterfaceName()
    {
        return interfaceName;
    }

    protected void setInterfaceName( QName _interfaceName )
    {
        interfaceName = _interfaceName;
    }

    public QName getOperationName()
    {
        return operationName;
    }

    protected void setOperationName( QName _operationName )
    {
        operationName = _operationName;
    }

    public String getEndpointName()
    {
        return endpointName;
    }

    public void setEndpointName( String endpointName )
    {
        this.endpointName = endpointName;
    }

    public void setAction( String action )
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }

    @Override
    public String toString()
    {
        return "{ class='" + super.getClass().getSimpleName() + "', serviceName: '" + serviceName + "', servcieUnit: '"
            + serviceUnit.getName() + "', operation: '" + operationName.getLocalPart() + "'}";
    }

    public void setReference( ReferenceEntry ref )
    {
        this.ref = ref;
    }

    public ReferenceEntry getReference()
    {
        return ref;
    }

    protected void setServiceUnit( ServiceUnit serviceUnit )
    {
        this.serviceUnit = serviceUnit;
    }

    public ServiceUnit getServiceUnit()
    {
        return serviceUnit;
    }

    public BindingOperation getBindingOperation()
    {
        return operation;
    }

    public XmlSchema getSchema()
    {
        return ( ( serviceUnit == null ) ? null : serviceUnit.getSchema( operationName.getLocalPart() ) );
    }

    public QName getInputElement()
    {
        return elementMap.get( "input" );
    }

    public QName getOutputElement()
    {
        return elementMap.get( "output" );
    }

    public QName getFaultElement( String name )
    {
        if ( name == null )
        {
            Set<String> keySet = elementMap.keySet();
            for ( String key : keySet )
            {
                if ( key.startsWith( "fault:" ) )
                {
                    return elementMap.get( key );
                }
            }
        }
        return elementMap.get( "fault:" + name );
    }
}