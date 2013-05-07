package com.sanxing.sesame.codec.impl;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.codec.util.CodecUtil;

public class DecodeFSVwithXML
    extends DecodeFSV
{
    @Override
    public Element decode( byte[] message, int length, String charset, XmlSchemaElement schemaElement, XmlSchema schema )
        throws FormatException
    {
        ByteBuffer msgBuf = ByteBuffer.wrap( message );
        Element root = null;
        try
        {
            if ( schemaElement == null )
            {
                throw new FormatException( "function decodeMessage,parameter  [schemaElement] is null!" );
            }
            XmlSchemaType xsdType = schemaElement.getSchemaType();
            if ( !( xsdType instanceof XmlSchemaComplexType ) )
            {
                throw new FormatException( "in xsdDoc,can not find the child element:[complexType]" );
            }
            root = new Element( schemaElement.getName() );

            Iterator elements = CodecUtil.getElements( xsdType );

            List fsvElements = new ArrayList();
            while ( elements.hasNext() )
            {
                XmlSchemaElement element = (XmlSchemaElement) elements.next();
                if ( CodecUtil.hasFormat( element, schema ) )
                {
                    fsvElements.add( element );
                }
            }

            decodeMessage( msgBuf, fsvElements.iterator(), charset, root, schema );

            byte[] xmlBuf = new byte[msgBuf.limit() - msgBuf.position()];
            msgBuf.get( xmlBuf );

            ByteArrayInputStream in = new ByteArrayInputStream( xmlBuf );

            SAXBuilder builder = new SAXBuilder();
            Document xmlDoc = builder.build( in );
            Element xmlRoot = xmlDoc.getRootElement();

            xmlRoot.detach();
            root.addContent( xmlRoot );
        }
        catch ( FormatException e )
        {
            e.printStackTrace();
            throw e;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new FormatException( e.getMessage(), e );
        }
        return root;
    }
}