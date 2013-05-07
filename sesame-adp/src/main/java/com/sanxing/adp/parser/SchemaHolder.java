package com.sanxing.adp.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.sanxing.adp.util.FileUtil;
import com.sanxing.adp.util.WSDLUtil;

public class SchemaHolder
{
    Definition def;

    private XmlSchema schema;

    private final Map<QName, XmlSchemaElement> types = new HashMap();

    private final Map declaredNamespaces;

    private final List<org.jdom.Element> jdomSchemaElements = new LinkedList();

    private String targetDir;

    private static Logger LOG = LoggerFactory.getLogger( SchemaHolder.class );

    public XmlSchema getSchema()
    {
        return schema;
    }

    public Map<QName, XmlSchemaElement> getTypes()
    {
        return types;
    }

    public SchemaHolder( Definition def )
        throws Exception
    {
        this.def = def;
        declaredNamespaces = def.getNamespaces();

        extractSchema( def );
    }

    private void extractSchema( Definition wsdlDefinition )
        throws Exception
    {
        org.w3c.dom.Element w3cSchemaElement = null;
        if ( wsdlDefinition.getTypes() != null )
        {
            List<ExtensibilityElement> schemaExtElements =
                WSDLUtil.findExtensibilityElement( wsdlDefinition.getTypes().getExtensibilityElements(), "schema" );
            for ( ExtensibilityElement schemaElement : schemaExtElements )
            {
                w3cSchemaElement = ( (SchemaImpl) schemaElement ).getElement();
                if ( w3cSchemaElement != null )
                {
                    DOMBuilder domBuilder = new DOMBuilder();
                    org.jdom.Element jdomSchemaElement = domBuilder.build( w3cSchemaElement );
                    jdom2Schema( jdomSchemaElement );
                }
            }
        }
        else
        {
            Map imports = wsdlDefinition.getImports();
            Iterator keys = imports.keySet().iterator();

            while ( keys.hasNext() )
            {
                Vector importV = (Vector) imports.get( keys.next() );
                Iterator importIter = importV.iterator();
                while ( importIter.hasNext() )
                {
                    Import impor = (Import) importIter.next();

                    declaredNamespaces.putAll( impor.getDefinition().getNamespaces() );
                    def = impor.getDefinition();

                    extractSchema( impor.getDefinition() );
                }
            }
        }
    }

    private void jdom2Schema( org.jdom.Element schemaElement )
        throws Exception
    {
        if ( ( declaredNamespaces != null ) && ( !( declaredNamespaces.isEmpty() ) ) )
        {
            Iterator nsIter = declaredNamespaces.keySet().iterator();
            while ( nsIter.hasNext() )
            {
                String nsPrefix = (String) nsIter.next();
                String nsURI = (String) declaredNamespaces.get( nsPrefix );
                if ( ( nsPrefix != null ) && ( nsPrefix.length() > 0 ) )
                {
                    Namespace nsDecl = Namespace.getNamespace( nsPrefix, nsURI );
                    schemaElement.addNamespaceDeclaration( nsDecl );
                }
            }
        }
        schemaElement.detach();
        jdomSchemaElements.add( schemaElement );
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();

        schemaCol.setBaseUri( FileUtil.getTargetDir( def.getDocumentBaseURI() ) );
        schema = schemaCol.read( new JDOMSource( new Document( schemaElement ) ), null );

        parseSchema( schema );
    }

    public List<org.jdom.Element> getSchemaJDOMElements()
    {
        return jdomSchemaElements;
    }

    private void parseSchema( XmlSchema schema )
        throws Exception
    {
        Iterator iterElementNames = schema.getElements().getValues();
        while ( iterElementNames.hasNext() )
        {
            XmlSchemaElement ele = (XmlSchemaElement) iterElementNames.next();
            types.put( ele.getQName(), ele );
        }
        XmlSchemaObjectCollection schemaCollection = schema.getIncludes();
        if ( schemaCollection != null )
        {
            Iterator iter = schemaCollection.getIterator();
            while ( iter.hasNext() )
            {
                XmlSchemaExternal include = (XmlSchemaExternal) iter.next();
                parseSchema( include.getSchema() );
            }
        }
    }
}