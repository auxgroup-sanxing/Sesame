package com.sanxing.sesame.test;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.component.ClientComponent;
import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.util.JdomUtil;
import com.sanxing.studio.utils.SchemaUtil;

public class UnitTest
{
    private final String serviceUnitRoot;

    private final String operationName;

    private Definition definition;

    private static Logger LOG = LoggerFactory.getLogger( UnitTest.class );

    public UnitTest( String serviceUnitRoot, String operationName )
    {
        this.operationName = operationName;
        this.serviceUnitRoot = serviceUnitRoot;
        definition = null;
        init();
    }

    public String generateXmlData()
    {
        String xml = "";
        try
        {
            String rootName = "";
            Iterator itr;
            Iterator iter;
            if ( definition != null )
            {
                Map ptMap = definition.getAllPortTypes();
                if ( ptMap != null )
                {
                    for ( itr = ptMap.entrySet().iterator(); itr.hasNext(); )
                    {
                        if ( !( "".equals( rootName ) ) )
                        {
                            break;
                        }
                        Map.Entry entry = (Map.Entry) itr.next();
                        PortType portType = (PortType) entry.getValue();
                        List opList = portType.getOperations();
                        if ( ( opList != null ) && ( !( opList.isEmpty() ) ) )
                        {
                            for ( iter = opList.iterator(); iter.hasNext(); )
                            {
                                Operation operation = (Operation) iter.next();
                                if ( operationName.equals( operation.getName() ) )
                                {
                                    Input input = operation.getInput();
                                    if ( input != null )
                                    {
                                        Message message = input.getMessage();
                                        rootName =
                                            ( message != null ) ? message.getPart( "parameters" ).getElementName().getLocalPart()
                                                : "";

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ( "".equals( rootName ) )
            {
                rootName = operationName;
            }

            File schemaFile = new File( serviceUnitRoot, operationName + ".xsd" );

            File flowFile = new File( serviceUnitRoot, operationName + ".xml" );

            Element xmlEle = null;
            if ( flowFile.exists() )
            {
                SAXBuilder builder = new SAXBuilder();
                Document xmldoc = builder.build( flowFile );
                xmlEle = xmldoc.getRootElement();
            }

            if ( schemaFile.exists() )
            {
                InputStream is = schemaFile.toURI().toURL().openStream();

                XmlSchemaCollection schemaCol = new XmlSchemaCollection();
                StreamSource source = new StreamSource( is );
                source.setSystemId( schemaFile.toURI().toString() );
                XmlSchema schema = schemaCol.read( source, null );

                Element xmlEl = SchemaUtil.schema2Xml( schema, rootName, xmlEle );
                Format format = Format.getPrettyFormat();
                format.setIndent( "  " );
                XMLOutputter outter = new XMLOutputter( format );
                xml = outter.outputString( xmlEl );
            }
            LOG.debug( xml );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            xml = "<request/>";
        }
        return xml;
    }

    public String sendRecv( String input )
    {
        try
        {
            QName serviceName = getServiceName( operationName );
            QName interfaceName = getInterfaceName( operationName );
            LOG.debug( "service: " + serviceName );
            LOG.debug( "interface: " + interfaceName );

            SAXBuilder builder = new SAXBuilder();
            Document doc = null;
            Reader in = new StringReader( input );
            doc = builder.build( in );
            Source inputSource = JdomUtil.JDOMDocument2DOMSource( doc );
            ClientComponent test_ac = ClientComponent.getInstance();

            MessageExchange exchange = test_ac.getExchangeFactory().createInOutExchange();
            Long serial = Long.valueOf( DummySequencer.getSerial() );
            exchange.setProperty( ExchangeConst.PLATFORM_SERIAL, serial );
            exchange.setService( serviceName );
            exchange.setInterfaceName( interfaceName );
            exchange.setOperation( new QName( operationName ) );
            NormalizedMessage msg = exchange.createMessage();
            msg.setContent( inputSource );
            exchange.setMessage( msg, ExchangeConst.IN );
            test_ac.sendSync( exchange );
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

            Source output = exchange.getMessage( ExchangeConst.OUT ).getContent();

            return JdomUtil.print( output );
        }
        catch ( Exception e )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "SendRecv error: " + e.getMessage(), e );
            }
            return e.getMessage();
        }
    }

    private void init()
    {
        try
        {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
            wsdlReader.setFeature( "javax.wsdl.verbose", false );
            wsdlReader.setFeature( "javax.wsdl.importDocuments", true );
            File wsdlFile = new File( serviceUnitRoot, "unit.wsdl" );
            definition = wsdlReader.readWSDL( wsdlFile.toURI().toString() );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    private QName getServiceName( String operationName )
    {
        Iterator itr;
        Service service;
        if ( definition != null )
        {
            Map sMap = definition.getServices();
            if ( sMap != null )
            {
                for ( itr = sMap.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    service = (Service) entry.getValue();
                    Map pMap = service.getPorts();
                    if ( pMap != null )
                    {
                        for ( Port port : (Collection<Port>) pMap.values() )
                        {
                            List bos = port.getBinding().getBindingOperations();
                            for ( int index = 0; index < bos.size(); ++index )
                            {
                                BindingOperation operation = (BindingOperation) bos.get( index );
                                if ( operationName.equals( operation.getName() ) )
                                {
                                    return service.getQName();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private QName getInterfaceName( String operationName )
    {
        Iterator itr;
        PortType portType;
        Iterator iter;
        if ( definition != null )
        {
            Map ptMap = definition.getAllPortTypes();
            if ( ptMap != null )
            {
                for ( itr = ptMap.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    portType = (PortType) entry.getValue();
                    List opList = portType.getOperations();
                    if ( ( opList != null ) && ( !( opList.isEmpty() ) ) )
                    {
                        for ( iter = opList.iterator(); iter.hasNext(); )
                        {
                            Operation operation = (Operation) iter.next();
                            if ( operationName.equals( operation.getName() ) )
                            {
                                return portType.getQName();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}