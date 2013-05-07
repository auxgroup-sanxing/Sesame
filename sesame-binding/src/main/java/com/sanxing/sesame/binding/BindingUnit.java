package com.sanxing.sesame.binding;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.service.ServiceUnit;

public class BindingUnit
{
    private static Logger LOG = LoggerFactory.getLogger( BindingUnit.class );

    private static Map<ServiceUnit, BindingUnit> units = new Hashtable();

    private final ServiceUnit unit;

    private Transport transport;

    private final Map<String, Map> messageTable = new Hashtable();

    public static BindingUnit newInstance( ServiceUnit serviceUnit )
        throws IOException
    {
        BindingUnit bindingUnit = units.get( serviceUnit );
        if ( bindingUnit != null )
        {
            return bindingUnit;
        }
        bindingUnit = new BindingUnit( serviceUnit );
        units.put( serviceUnit, bindingUnit );
        return bindingUnit;
    }

    private BindingUnit( ServiceUnit unit )
        throws IOException
    {
        this.unit = unit;
    }

    public Definition getDefinition()
    {
        return unit.getDefinition();
    }

    public QName getServiceName()
    {
        return unit.getServiceName();
    }

    public Service getService()
    {
        return unit.getService();
    }

    public String getServiceUnitName()
    {
        return unit.getDefinition().getQName().getLocalPart();
    }

    public String getLocation()
    {
        Port port = null;
        Service service = unit.getService();
        Collection ports = service.getPorts().values();
        if ( ports.isEmpty() )
        {
            return null;
        }
        port = (Port) ports.iterator().next();
        Iterator iter = port.getExtensibilityElements().iterator();
        if ( !( iter.hasNext() ) )
        {
            return null;
        }

        ExtensibilityElement extEl = (ExtensibilityElement) iter.next();
        String location;
        if ( extEl instanceof SOAPAddress )
        {
            SOAPAddress el = (SOAPAddress) extEl;
            location = el.getLocationURI();
        }
        else
        {
            UnknownExtensibilityElement el = (UnknownExtensibilityElement) extEl;
            location = el.getElement().getAttribute( "location" );
        }
        return location;
    }

    public XmlSchema getSchema( String operationName )
    {
        return unit.getSchema( operationName );
    }

    public XmlSchemaCollection getSchemaCollection()
    {
        return unit.getSchemaCollection();
    }

    public ServiceUnit getServiceUnit()
    {
        return unit;
    }

    public Transport getTransport()
    {
        return transport;
    }

    public File getUnitRoot()
    {
        return unit.getUnitRoot();
    }

    public void destroy()
    {
        messageTable.clear();

        for ( Map.Entry entry : units.entrySet() )
        {
            if ( ( (BindingUnit) entry.getValue() ).equals( this ) )
            {
                units.remove( entry.getKey() );
                return;
            }
        }
    }

    public void setTransport( Transport transport )
    {
        this.transport = transport;
    }
}