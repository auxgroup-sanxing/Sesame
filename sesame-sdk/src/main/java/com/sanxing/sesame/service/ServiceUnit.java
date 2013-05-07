package com.sanxing.sesame.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.jbi.JBIException;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.util.W3CUtil;
import com.sanxing.sesame.util.WSDLLocatorImpl;

public class ServiceUnit
{
    private static Logger LOG = LoggerFactory.getLogger( ServiceUnit.class );

    private final File serviceUnitRoot;

    private Definition definition;

    private Document descriptor;

    private final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

    private final Map<String, XmlSchema> schemaMap = new HashMap();

    private final Map<QName, ReferenceEntry> references = new HashMap();

    private final Map<String, OperationContext> operationMap = new HashMap();

    public QName getServiceName()
    {
        Service service = getService();
        return ( ( service == null ) ? null : service.getQName() );
    }

    public Service getService()
    {
        Map services = definition.getServices();
        if ( services.isEmpty() )
        {
            return null;
        }
        return ( (Service) services.values().iterator().next() );
    }

    public Definition getDefinition()
    {
        return definition;
    }

    public ServiceUnit( File serviceUnitRoot )
        throws JBIException
    {
        this.serviceUnitRoot = serviceUnitRoot;
        try
        {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
            wsdlReader.setFeature( "javax.wsdl.verbose", false );
            wsdlReader.setFeature( "javax.wsdl.importDocuments", true );
            File unitFile = new File( serviceUnitRoot, "unit.wsdl" );
            WSDLLocator locator = new WSDLLocatorImpl( unitFile );
            definition = wsdlReader.readWSDL( locator );

            File jbiFile = new File( serviceUnitRoot, "META-INF/jbi.xml" );
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            descriptor = builder.parse( jbiFile );

            loadSchemas( definition );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Schema map of '" + serviceUnitRoot.getName() + "' ..." );
                for ( Map.Entry entry : schemaMap.entrySet() )
                {
                    LOG.debug( ( (String) entry.getKey() ) + " -> XmlSchema@"
                        + ( (XmlSchema) entry.getValue() ).hashCode() );
                }
                LOG.debug( "--------------------------------------------------------------------------------" );
            }

            loadOperations();
        }
        catch ( Exception e )
        {
            throw new JBIException( e.getMessage(), e );
        }
    }

    public String getExtentionAttribute( String attributeName )
    {
        NodeList nodes = descriptor.getElementsByTagName( "services" );
        if ( nodes.getLength() > 0 )
        {
            Element elem = (Element) nodes.item( 0 );
            return elem.getAttribute( "art:" + attributeName );
        }

        return "";
    }

    public NodeList getExtentionElements( String operationName )
        throws Exception
    {
        NodeList nodes = descriptor.getElementsByTagName( "art:link" );
        int i = 0;
        for ( int len = nodes.getLength(); i < len; ++i )
        {
            Element elem = (Element) nodes.item( i );
            if ( elem.getAttribute( "operation-name" ).equals( operationName ) )
            {
                return elem.getElementsByTagName( "*" );
            }
        }
        return null;
    }

    private void loadSchemas( Definition wsdlDef )
        throws IOException
    {
        Map imports = wsdlDef.getImports();
        for ( Iterator iter = imports.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Vector<Import> victor = (Vector<Import>) entry.getValue();
            for ( Import wsdlImport : victor )
            {
                loadSchemas( wsdlImport.getDefinition() );
            }
        }

        if ( wsdlDef.getTypes() == null )
        {
            return;
        }

        List list = wsdlDef.getTypes().getExtensibilityElements();
        if ( !( list.isEmpty() ) )
        {
            ExtensibilityElement extEl = (ExtensibilityElement) list.get( 0 );
            Element schemaEl =
                ( extEl instanceof Schema ) ? ( (Schema) extEl ).getElement()
                    : ( (UnknownExtensibilityElement) extEl ).getElement();

            XmlSchema schema = schemaCollection.read( schemaEl, wsdlDef.getDocumentBaseURI() );
            Iterator it = schema.getItems().getIterator();
            while ( it.hasNext() )
            {
                Object item = it.next();
                if ( item instanceof XmlSchemaInclude )
                {
                    XmlSchemaInclude xsi = (XmlSchemaInclude) item;
                    File file = new File( xsi.getSchemaLocation() );
                    String opera = file.getName().replaceAll( "\\..*$", "" );

                    schemaMap.put( opera, xsi.getSchema() );
                }
            }
        }
    }

    public XmlSchemaCollection getSchemaCollection()
    {
        return schemaCollection;
    }

    public XmlSchema getSchema( String operationName )
    {
        return schemaMap.get( operationName );
    }

    public OperationContext getOperationContext( String operationName )
    {
        return operationMap.get( operationName );
    }

    public Collection<OperationContext> getOperationContexts()
    {
        return operationMap.values();
    }

    private void loadOperations()
    {
        NodeList items = descriptor.getElementsByTagName( "art:link" );
        String serviceName;
        for ( int i = 0; i < items.getLength(); ++i )
        {
            Element el = (Element) items.item( 0 );
            ReferenceEntry ref = new ReferenceEntry();
            ref.setEndpointName( W3CUtil.getChildText( el, "endpoint-name" ) );
            serviceName = W3CUtil.getChildText( el, "service-name" );
            if ( serviceName != null )
            {
                ref.setServcieName( QName.valueOf( serviceName ) );
            }
            String interfaceName = W3CUtil.getChildText( el, "interface-name" );
            if ( interfaceName != null )
            {
                ref.setInterfaceName( QName.valueOf( interfaceName ) );
            }
            ref.setOperationName( QName.valueOf( W3CUtil.getChildText( el, "operation-name" ) ) );
            references.put( QName.valueOf( el.getAttribute( "operation-name" ) ), ref );
        }

        Set<Map.Entry> services = definition.getServices().entrySet();
        Map.Entry intfEntry;
        if ( services.isEmpty() )
        {
            Operation operation;
            Set portTypes = definition.getPortTypes().entrySet();
            for ( Iterator ite = portTypes.iterator(); ite.hasNext(); )
            {
                intfEntry = (Map.Entry) ite.next();
                PortType portType = (PortType) intfEntry.getValue();
                List operations = portType.getOperations();
                for ( Iterator localIterator1 = operations.iterator(); localIterator1.hasNext(); )
                {
                    operation = (Operation) localIterator1.next();
                    if ( operation.getName() == null )
                    {
                        continue;
                    }
                    OperationContext context = new OperationContext( operation );
                    context.setInterfaceName( portType.getQName() );
                    context.setServiceUnit( this );
                    operationMap.put( operation.getName(), context );
                }
            }
        }
        else
        {
            for ( Map.Entry serviceEntry : services )
            {
                Service service = (Service) serviceEntry.getValue();
                Set<Map.Entry> ports = service.getPorts().entrySet();

                for ( Map.Entry portEntry : ports )
                {
                    Port port = (Port) portEntry.getValue();
                    Binding binding = port.getBinding();
                    if ( binding == null )
                    {
                        continue;
                    }
                    List<BindingOperation> operations = binding.getBindingOperations();
                    for ( BindingOperation operation : operations )
                    {
                        if ( operation.getName() == null )
                        {
                            continue;
                        }
                        OperationContext context = new OperationContext( operation );
                        PortType portType = binding.getPortType();
                        QName operationName = context.getOperationName();
                        ReferenceEntry ref = references.get( operationName );
                        context.setReference( ref );
                        context.setServiceUnit( this );
                        context.setInterfaceName( ( portType == null ) ? null : portType.getQName() );
                        context.setServcieName( service.getQName() );
                        context.setEndpointName( port.getName() );
                        operationMap.put( operation.getName(), context );
                    }
                }
            }
        }
    }

    public ReferenceEntry getReference( QName operationName )
    {
        return references.get( operationName );
    }

    public URL getResource( String name )
        throws IOException
    {
        File file = new File( serviceUnitRoot, name ).getCanonicalFile();
        return ( ( file.exists() ) ? file.toURI().toURL() : null );
    }

    public File getUnitRoot()
    {
        return serviceUnitRoot;
    }

    public String getName()
    {
        return serviceUnitRoot.getName();
    }
}