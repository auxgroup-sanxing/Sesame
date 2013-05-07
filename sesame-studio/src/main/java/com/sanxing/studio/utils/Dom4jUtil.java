package com.sanxing.studio.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

public class Dom4jUtil
{
    static org.dom4j.Document document;

    static final String XML_DEFAULT_ENCODE = "UTF-8";

    public static final String ACTION_URL = "http://www.webserviceX.NET/";

    static final String XPATH_PRE = "//*[name()='";

    static final String XPATH_NCPRE = "//*[local-name()='";

    static final String XPATH_MID = "']/*[name()='";

    static final String XPATH_NCMID = "']/*[local-name()='";

    static final String XPATH_END = "']";

    static final String ELEM_DEFINITIONS = "definitions";

    static final String ELEM_IMPORT = "import";

    static final String ELEM_TYPES = "types";

    static final String ELEM_MESSAGE = "message";

    static final String ELEM_PORT_TYPE = "portType";

    static final String ELEM_BINDING = "binding";

    static final String ELEM_SERVICE = "service";

    static final String ELEM_PART = "part";

    static final String ELEM_OPERATION = "operation";

    static final String ELEM_INPUT = "input";

    static final String ELEM_OUTPUT = "output";

    static final String ELEM_FAULT = "fault";

    static final String ELEM_PORT = "port";

    static final String ELEM_SCHEMA = "schema";

    static final String ELEM_DOCUMENTATION = "documentation";

    static final String ELEM_INCLUDE = "include";

    static final String ATTR_NAME = "name";

    static final String ATTR_TARGET_NAMESPACE = "targetNamespace";

    static final String ATTR_ELEMENT = "element";

    static final String ATTR_TYPE = "type";

    static final String ATTR_MESSAGE = "message";

    static final String ATTR_PARAMETER_ORDER = "parameterOrder";

    static final String ATTR_BINDING = "binding";

    static final String ATTR_XMLNS = "xmlns";

    static final String ATTR_NAMESPACE = "namespace";

    static final String ATTR_LOCATION = "location";

    static final String ATTR_REQUIRED = "required";

    static final String ATTR_XPATH = "xpath";

    static final String ATTR_HEAD = "head";

    static final String ATTR_CMP_NAME = "component-name";

    static final String ATTR_TRANS = "transport";

    static final String ELEM_SERVICES = "services";

    static final String ELEM_LINK = "link";

    public static void initDocument( String path )
        throws DocumentException
    {
        File xmlFile = new File( path );
        SAXReader saxReader = new SAXReader();
        Map map = new HashMap();
        map.put( "wsdl", "http://schemas.xmlsoap.org/wsdl/" );
        map.put( "soap", "http://schemas.xmlsoap.org/wsdl/soap/" );
        map.put( "xs", "http://www.w3.org/2001/XMLSchema" );
        map.put( "sn", "http://www.sanxing.com/ns/sesame" );
        saxReader.getDocumentFactory().setXPathNamespaceURIs( map );
        saxReader.setEncoding( "UTF-8" );
        document = saxReader.read( xmlFile );
    }

    public static void initDocument( File file )
        throws DocumentException
    {
        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding( "UTF-8" );
        document = saxReader.read( file );
    }

    public static org.dom4j.Element getRootEl()
    {
        return document.getRootElement();
    }

    public static String getName()
    {
        String name = "";
        if ( document != null )
        {
            name = getAttributeValue( getRootEl(), "name" );
        }
        return name;
    }

    public static String getNameSpace()
    {
        String ns = "";
        if ( document != null )
        {
            ns = getAttributeValue( getRootEl(), "targetNamespace" );
        }
        return ns;
    }

    public static org.dom4j.Element createEl( org.dom4j.Element el, String elName )
    {
        org.dom4j.Element child = el.addElement( elName );
        return child;
    }

    public static org.dom4j.Element getElement( org.dom4j.Element parent, String elName )
    {
        org.dom4j.Element el = null;
        List list = parent.elements();
        Iterator itr = list.iterator();
        do
        {
            if ( !( itr.hasNext() ) )
            {
                break;
            }
            el = (org.dom4j.Element) itr.next();
        }
        while ( !( elName.equals( el.getName() ) ) );

        return el;
    }

    public static org.w3c.dom.Element parseDOM4JElement2DOMElement( org.dom4j.Element dom4jElement )
        throws Exception
    {
        org.w3c.dom.Element w3cElement = parseDOM4JDocument2Document( dom4jElement.getDocument() ).getDocumentElement();
        return w3cElement;
    }

    public static org.dom4j.Element parseDOMElement2DOM4JElement( org.w3c.dom.Element w3cElement )
        throws Exception
    {
        org.dom4j.Element dom4jElement = parseDOM2DOM4JDocument( w3cElement.getOwnerDocument() ).getRootElement();
        return dom4jElement;
    }

    public static org.dom4j.Document parseDOM2DOM4JDocument( org.w3c.dom.Document doc )
        throws Exception
    {
        if ( doc == null )
        {
            return null;
        }
        DOMReader xmlReader = new DOMReader();
        return xmlReader.read( doc );
    }

    public static org.w3c.dom.Document parseDOM4JDocument2Document( org.dom4j.Document doc )
        throws Exception
    {
        if ( doc == null )
        {
            return null;
        }
        StringReader reader = new StringReader( doc.asXML() );
        InputSource source = new InputSource( reader );
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse( source );
    }

    public static String getAttributeValue( org.dom4j.Element el, String attrName )
    {
        return el.attributeValue( attrName, "" );
    }

    public static void setAttributeValue( org.dom4j.Element el, String attrName, String attrValue )
    {
        Attribute attr = el.attribute( attrName );

        if ( attr != null )
        {
            if ( !( attrValue.equals( attr.getText() ) ) )
            {
                attr.setText( attrValue );
            }
            else
            {
                el.addAttribute( attrName, attrValue );
            }
        }
    }

    public static String getDocumentation()
    {
        String value = "";
        org.dom4j.Element root = getRootEl();
        value = ( root.element( "documentation" ) != null ) ? root.element( "documentation" ).getText() : "";
        return value;
    }

    public static void setDocumentation( String docName )
    {
        org.dom4j.Element root = getRootEl();
        String value = root.element( "documentation" ).getText();
        if ( !( docName.equals( value ) ) )
        {
            root.element( "documentation" ).setText( docName );
        }
    }

    public static void addNamespace( org.dom4j.Element el, String prefix, String uri )
    {
        removeNamespace( el, prefix );
        el.addNamespace( prefix, uri );
    }

    public static void removeNamespace( org.dom4j.Element el, String prefix )
    {
        org.dom4j.Element root = getRootEl();
        Namespace ns = root.getNamespaceForPrefix( prefix );
        el.remove( ns );
    }

    public static org.dom4j.Document strToXml( String xmlStr )
    {
        SAXReader saxReader = new SAXReader();
        org.dom4j.Document document = null;
        try
        {
            document = saxReader.read( new ByteArrayInputStream( xmlStr.getBytes( "UTF-8" ) ) );
        }
        catch ( Exception e )
        {
            return null;
        }
        return document;
    }

    public static void saveFile( String xmlPath )
        throws IOException
    {
        OutputFormat form = OutputFormat.createPrettyPrint();
        form.setEncoding( "UTF-8" );

        XMLWriter output = new XMLWriter( new FileOutputStream( xmlPath ), form );

        output.write( document );
        output.close();
    }

    public static void saveFile( File xmlPath )
        throws IOException
    {
        OutputFormat form = OutputFormat.createPrettyPrint();
        form.setEncoding( "UTF-8" );

        XMLWriter output = new XMLWriter( new FileOutputStream( xmlPath ), form );

        output.write( document );
        output.close();
    }

    public static class JBI
    {
        public static org.dom4j.Element getRoot()
        {
            org.dom4j.Element root = Dom4jUtil.document.getRootElement();
            return root;
        }

        public static org.dom4j.Element getServices()
        {
            org.dom4j.Element consumes =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='services']" );

            return consumes;
        }

        public static List getArtLinkList()
        {
            List operList = Dom4jUtil.document.selectNodes( "//*[name()='services']/*[local-name()='link']" );

            return operList;
        }
    }

    public static class PortType
    {
        public static String getName()
        {
            org.dom4j.Element ptEl = (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='portType']" );

            if ( ptEl == null )
            {
                return "";
            }
            String name = Dom4jUtil.getAttributeValue( ptEl, "name" );
            return name;
        }

        public static List getOperList()
        {
            List operList = Dom4jUtil.document.selectNodes( "//*[name()='portType']/*[name()='operation']" );

            return operList;
        }

        public static org.dom4j.Element addOperation( String intfName )
        {
            org.dom4j.Element root = (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='portType']" );
            org.dom4j.Element operEl = null;
            if ( root == null )
            {
                root = Dom4jUtil.getRootEl().addElement( "portType" );
                root.addAttribute( "name", intfName );
            }
            operEl = root.addElement( "operation" );
            return operEl;
        }

        public static List getPortTypeList()
        {
            List portTypeList = Dom4jUtil.document.selectNodes( "//*[name()='portType']" );

            return portTypeList;
        }

        public static String getName( org.dom4j.Element portType )
        {
            String name = Dom4jUtil.getAttributeValue( portType, "name" );
            return name;
        }

        public static List getOperList( org.dom4j.Element portType )
        {
            List operList = portType.selectNodes( "//*[name()='operation']" );

            return operList;
        }

        public static org.dom4j.Element addOperation( org.dom4j.Element portType, String intfName )
        {
            org.dom4j.Element root = portType;
            org.dom4j.Element operEl = null;
            if ( root == null )
            {
                root = Dom4jUtil.getRootEl().addElement( "portType" );
                root.addAttribute( "name", intfName );
            }
            operEl = root.addElement( "operation" );
            return operEl;
        }

        public static org.dom4j.Element createPutElement( org.dom4j.Element operation, String elName, String msgValue )
        {
            org.dom4j.Element childEl = operation.element( elName );
            if ( childEl != null )
            {
                operation.remove( childEl );
            }
            org.dom4j.Element el = operation.addElement( elName );
            el.addAttribute( "message", msgValue );
            return el;
        }

        public static String getElementValue( org.dom4j.Element operation, String elName )
        {
            String value = "";
            List list = operation.elements();
            for ( Iterator itr = list.iterator(); itr.hasNext(); )
            {
                org.dom4j.Element el = (org.dom4j.Element) itr.next();
                if ( elName.equals( el.getName() ) )
                {
                    value = el.getText();
                    break;
                }
            }
            return value;
        }
    }

    public static class Service
    {
        public static org.dom4j.Element getService()
        {
            org.dom4j.Element serviceEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='service']" );

            if ( serviceEl == null )
            {
                serviceEl = Dom4jUtil.getRootEl().addElement( "service" );
            }
            return serviceEl;
        }

        public static String getName()
        {
            org.dom4j.Element serviceEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='service']" );

            if ( serviceEl == null )
            {
                return "";
            }
            String name = Dom4jUtil.getAttributeValue( serviceEl, "name" );
            return name;
        }

        public static void setName( String name )
        {
            org.dom4j.Element serviceEl = getService();
            serviceEl.addAttribute( "name", name );
        }

        public static List getPortList()
        {
            List ports = new ArrayList();
            ports = Dom4jUtil.document.selectNodes( "//*[name()='service']/*[name()='port']" );

            return ports;
        }

        public static String getPortAttrValue( org.dom4j.Element port, String attrName )
        {
            String atrValue = "";
            List attrs = port.attributes();
            Iterator itr = attrs.iterator();
            while ( itr.hasNext() )
            {
                Attribute atr = (Attribute) itr.next();
                if ( attrName.equals( atr.getName() ) )
                {
                    atrValue = atr.getValue();
                    break;
                }
            }
            return atrValue;
        }
    }

    public static class Binding
    {
        public static org.dom4j.Element getRoot()
        {
            org.dom4j.Element bindingEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']" );

            return bindingEl;
        }

        public static String getName()
        {
            org.dom4j.Element bindingEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']" );

            String name = ( bindingEl == null ) ? "" : Dom4jUtil.getAttributeValue( bindingEl, "name" );
            return name;
        }

        public static String getType()
        {
            org.dom4j.Element bindingEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']" );

            String type = ( bindingEl == null ) ? "" : Dom4jUtil.getAttributeValue( bindingEl, "type" );
            return type;
        }

        public static List getOperList()
        {
            List operList = Dom4jUtil.document.selectNodes( "//*[name()='binding']/*[name()='operation']" );

            return operList;
        }

        public static String getComponentName()
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String componentName =
                ( bindingChildEl == null ) ? "" : Dom4jUtil.getAttributeValue( bindingChildEl, "component-name" );

            return componentName;
        }

        public static String getTransport()
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String transport =
                ( bindingChildEl == null ) ? "" : Dom4jUtil.getAttributeValue( bindingChildEl, "transport" );
            return transport;
        }

        public static String getActionType()
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String type = ( bindingChildEl == null ) ? "" : Dom4jUtil.getAttributeValue( bindingChildEl, "type" );
            return type;
        }

        public static String getXpath()
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String type = ( bindingChildEl == null ) ? "" : bindingChildEl.attributeValue( "xpath", "" );
            return type;
        }

        public static String getHead()
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String type = ( bindingChildEl == null ) ? "" : bindingChildEl.attributeValue( "head", "" );
            return type;
        }

        public static List getBindingList()
        {
            List bindingList = Dom4jUtil.document.selectNodes( "//*[name()='binding']" );

            return bindingList;
        }

        public static String getName( org.dom4j.Element bindingEl )
        {
            String name = ( bindingEl != null ) ? Dom4jUtil.getAttributeValue( bindingEl, "name" ) : "";
            return name;
        }

        public static String getType( org.dom4j.Element bindingEl )
        {
            String type = ( bindingEl != null ) ? Dom4jUtil.getAttributeValue( bindingEl, "type" ) : "";
            return type;
        }

        public static List getOperList( org.dom4j.Element bindingEl )
        {
            List operList = bindingEl.selectNodes( "//*[name()='operation']" );

            return operList;
        }

        public static org.dom4j.Element getElement( org.dom4j.Element operation, String elName )
        {
            org.dom4j.Element el = null;
            List list = operation.elements();
            Iterator itr = list.iterator();
            do
            {
                if ( !( itr.hasNext() ) )
                {
                    break;
                }
                el = (org.dom4j.Element) itr.next();
            }
            while ( !( elName.equals( el.getName() ) ) );

            return el;
        }

        public static String getComponentName( org.dom4j.Element bindingEl )
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) bindingEl.selectSingleNode( "//*[local-name()='binding']" );

            String componentName =
                ( bindingChildEl == null ) ? "" : Dom4jUtil.getAttributeValue( bindingChildEl, "component-name" );
            return componentName;
        }

        public static String getTransport( org.dom4j.Element bindingEl )
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            if ( bindingChildEl == null )
            {
                return "";
            }
            String transport = Dom4jUtil.getAttributeValue( bindingChildEl, "transport" );
            return transport;
        }

        public static String getActionType( org.dom4j.Element bindingEl )
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String type = ( bindingChildEl == null ) ? "" : Dom4jUtil.getAttributeValue( bindingChildEl, "type" );
            return type;
        }

        public static String getXpath( org.dom4j.Element bindingEl )
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String type = ( bindingChildEl == null ) ? "" : bindingChildEl.attributeValue( "xpath", "" );
            return type;
        }

        public static String getHead( org.dom4j.Element bindingEl )
        {
            org.dom4j.Element bindingChildEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='binding']/*[local-name()='binding']" );

            String type = ( bindingChildEl == null ) ? "" : bindingChildEl.attributeValue( "head", "" );
            return type;
        }
    }

    public static class Message
    {
        public static void initMessage()
        {
            List msgList = Dom4jUtil.document.selectNodes( "//*[name()='message']" );

            if ( msgList != null )
            {
                for ( int i = 0; i < msgList.size(); ++i )
                {
                    org.dom4j.Element el = (org.dom4j.Element) msgList.get( i );
                    Dom4jUtil.getRootEl().remove( el );
                }
            }
        }

        public static List getMessageList()
        {
            List msgList = Dom4jUtil.document.selectNodes( "//*[name()='message']" );

            return msgList;
        }

        public static void modifyMessage( String oldName, String operName )
        {
            List msgList = getMessageList();
            if ( ( msgList == null ) || ( msgList.size() == 0 ) )
            {
                return;
            }
            for ( int i = 0; i < msgList.size(); ++i )
            {
                org.dom4j.Element msg = (org.dom4j.Element) msgList.get( i );
                String name = Dom4jUtil.getAttributeValue( msg, "name" ).replaceAll( "(R|r)(equest|esponse)", "" );
                if ( name.equals( oldName ) )
                {
                    msg.addAttribute( "name", name.replaceAll( oldName, operName ) );

                    org.dom4j.Element port = Dom4jUtil.getElement( msg, "port" );
                    String type = ( port == null ) ? "" : Dom4jUtil.getAttributeValue( port, "type" );
                    String portname = ( port == null ) ? "" : Dom4jUtil.getAttributeValue( port, "name" );
                    port.addAttribute( "type", type.replaceAll( oldName, operName ) );
                    port.addAttribute( "name", portname.replaceAll( oldName, operName ) );
                }
            }
        }

        public static void removeMessage( String operName )
        {
            List msgList = getMessageList();
            if ( ( msgList == null ) || ( msgList.size() == 0 ) )
            {
                return;
            }
            for ( int i = 0; i < msgList.size(); ++i )
            {
                org.dom4j.Element msg = (org.dom4j.Element) msgList.get( i );
                String name = Dom4jUtil.getAttributeValue( msg, "name" ).replaceAll( "(R|r)(equest|esponse)", "" );

                if ( operName.equals( name ) )
                {
                    msg.getParent().remove( msg );
                }
            }
        }

        public static org.dom4j.Element createMessage( String operName, boolean isRequest )
        {
            org.dom4j.Element msgEl = Dom4jUtil.getRootEl().addElement( "message" );
            String tmp = ( isRequest ) ? "Request" : "Response";
            String name = operName + tmp;
            msgEl.addAttribute( "name", name );

            org.dom4j.Element partEl = msgEl.addElement( "part" );
            partEl.addAttribute( "name", "parameters" );
            partEl.addAttribute( "type", "tns:" + name );
            return msgEl;
        }
    }

    public static class Types
    {
        public static org.dom4j.Element getSchema()
        {
            org.dom4j.Element schemaEl =
                (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='types']/*[local-name()='schema']" );

            if ( schemaEl == null )
            {
                org.dom4j.Element typesEl =
                    (org.dom4j.Element) Dom4jUtil.document.selectSingleNode( "//*[name()='types']" );

                if ( typesEl == null )
                {
                    typesEl = Dom4jUtil.getRootEl().addElement( "types" );
                }
                schemaEl =
                    typesEl.addElement( new QName( "schema", Namespace.get( "xs", "http://www.w3.org/2001/XMLSchema" ) ) );
            }
            return schemaEl;
        }

        public static List getIncludeList()
        {
            List includeList =
                Dom4jUtil.document.selectNodes( "//*[name()='types']/*[local-name()='schema']/*[local-name()='include']" );

            return includeList;
        }
    }
}