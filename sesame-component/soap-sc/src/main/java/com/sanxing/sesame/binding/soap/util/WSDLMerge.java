package com.sanxing.sesame.binding.soap.util;

import com.ibm.wsdl.TypesImpl;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.sanxing.sesame.address.AddressBook;
import com.sanxing.sesame.address.Location;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaSerializer.XmlSchemaSerializerException;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLMerge
{
    private static final Logger log = LoggerFactory.getLogger( WSDLMerge.class );

    public Definition merge( Definition source )
        throws XmlSchemaSerializerException, Exception
    {
        cleanNamespace( source );

        combineImport( source );

        getRealURI( source );

        return source;
    }

    private void getRealURI( Definition source )
        throws IOException, URISyntaxException
    {
        // TODO
    }

    private void cleanNamespace( Definition source )
    {
        // TODO
    }

    private void combineImport( Definition source )
        throws Exception
    {
        // TODO
    }

    private void moveSchema( Definition source, Schema importSchema )
        throws XmlSchemaSerializerException
    {
        Element schemaElement = importSchema.getElement();
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        schemaCol.read( schemaElement, schemaElement.getBaseURI() );
        for ( int j = 0; j < schemaCol.getXmlSchemas().length; j++ )
        {
            try
            {
                XmlSchema schema = schemaCol.getXmlSchemas()[j];
                NamespaceMap namespaceContext = new NamespaceMap();
                namespaceContext.add( "", "http://www.w3.org/2001/XMLSchema" );
                namespaceContext.add( "tns", schema.getTargetNamespace() );
                schema.setNamespaceContext( namespaceContext );
                SchemaImpl wsdlSchema = new SchemaImpl();
                Element w3cdoc4schema = schema.getSchemaDocument().getDocumentElement();
                if ( w3cdoc4schema.getChildNodes().getLength() > 1 )
                {
                    source.getTypes().addExtensibilityElement( wsdlSchema );
                }
                NodeList importList = w3cdoc4schema.getElementsByTagName( "import" );
                while ( importList.getLength() > 0 )
                {
                    Node node = importList.item( 0 );
                    node.getParentNode().removeChild( node );
                }
                NodeList includeList = w3cdoc4schema.getElementsByTagName( "include" );
                while ( includeList.getLength() > 0 )
                {
                    Node node = includeList.item( 0 );
                    node.getParentNode().removeChild( node );
                }
                wsdlSchema.setElement( w3cdoc4schema );

                wsdlSchema.setElementType( new QName( "http://www.w3.org/2001/XMLSchema", "schema" ) );
            }
            catch ( XmlSchemaSerializerException e )
            {
                throw e;
            }
        }
    }
}