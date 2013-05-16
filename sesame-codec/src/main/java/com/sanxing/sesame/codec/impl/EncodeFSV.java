package com.sanxing.sesame.codec.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAppInfo;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLSource;
import com.sanxing.sesame.codec.impl.validate.ValidateFFormat;
import com.sanxing.sesame.codec.impl.validate.ValidateField;
import com.sanxing.sesame.codec.impl.validate.ValidateVFormat;
import com.sanxing.sesame.codec.util.CodecUtil;
import com.sanxing.sesame.codec.util.HexBinary;
import com.sanxing.sesame.codec.util.RidFillBlank;
import com.sanxing.sesame.util.JdomUtil;

public class EncodeFSV
    implements Encoder
{
    private static Logger LOG = LoggerFactory.getLogger( EncodeFSV.class );

    private final Map<String, Integer> partTypeMap = new HashMap();

    private final List<String> typeList = new ArrayList();

    public EncodeFSV()
    {
        partTypeMap.put( "F", new Integer( 0 ) );
        partTypeMap.put( "S", new Integer( 1 ) );
        partTypeMap.put( "V", new Integer( 2 ) );
        typeList.add( "int" );
        typeList.add( "unsignedInt" );
        typeList.add( "short" );
        typeList.add( "unsignedShort" );
        typeList.add( "long" );
        typeList.add( "unsignedLong" );
        typeList.add( "float" );
        typeList.add( "double" );
        typeList.add( "hexBinary" );
        typeList.add( "decimal" );
        typeList.add( "byte" );
    }

    @Override
    public void encode( XMLSource source, BinaryResult result )
        throws FormatException
    {
        String charset = result.getEncoding();
        XmlSchema schema = result.getXMLSchema();

        if (LOG.isDebugEnabled()) 
        {
            LOG.debug( "------------debug xmlschema start" );
            Iterator iter = schema.getElements().getNames();
            while ( iter.hasNext() )
            {
                LOG.debug( iter.next().toString() );
            }
            LOG.debug( "------------debug xmlschema finish" );
        }

        OutputStream output = result.getOutputStream();
        String elementName = result.getElementName();

        if ( charset == null )
        {
            throw new FormatException( "charset not specified!" );
        }
        try
        {
            Document message = source.getJDOMDocument();
            if ( message == null )
            {
                throw new FormatException( "can not get document from parameter [XMLSource]!" );
            }
            message.getRootElement().setName( elementName );

            encode( message.getRootElement(), schema, output, charset );
        }
        catch ( FormatException fe )
        {
            throw fe;
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public void encode( org.jdom.Element xmlElement, XmlSchema schemaElement, OutputStream output, String encodeCharset )
        throws FormatException
    {
        if ( xmlElement == null )
        {
            throw new FormatException( "the source message root is null!" );
        }
        if ( schemaElement.getElementByName( xmlElement.getName() ) == null )
        {
            throw new FormatException( "can not get schema element by name:[" + xmlElement.getName() + "]!" );
        }
        XmlSchemaType xsdType = schemaElement.getElementByName( xmlElement.getName() ).getSchemaType();
        LOG.debug( "-----------------schema xml=" + xmlElement.getName() );
        if ( !( xsdType instanceof XmlSchemaComplexType ) )
        {
            throw new FormatException( "in xsdDoc,can not find the child element:[complexType]!" );
        }
        encodeMessage( CodecUtil.getElements( xsdType ), xmlElement, output, encodeCharset, schemaElement );
    }

    protected void encodeMessage( Iterator elements, org.jdom.Element xmlElement, OutputStream output,
                                  String encodeCharset, XmlSchema schema )
        throws FormatException
    {
        try
        {
            while ( elements.hasNext() )
            {
                XmlSchemaElement element = (XmlSchemaElement) elements.next();
                String elementName = element.getName();
                LOG.debug( "encode element: " + elementName );
                if ( "".equals( elementName ) )
                {
                    throw new FormatException(
                        "in xsdDoc,element [request] or [response],the attribute [name] do not have value" );
                }
                XmlSchemaType type = null;
                if ( element.getRefName() != null )
                {
                    if ( schema.getElementByName( element.getRefName() ) == null )
                    {
                        throw new FormatException( "can not get element from schema by name:[" + element.getRefName()
                            + "]!" );
                    }
                    type = schema.getElementByName( element.getRefName() ).getSchemaType();
                }
                else
                {
                    type = element.getSchemaType();
                }

                if ( element.getRefName() != null )
                {
                    element = schema.getElementByName( element.getRefName() );
                }
                org.w3c.dom.Element format;
                String elementType;
                String elementValue;
                int kindInt;
                if ( type instanceof XmlSchemaSimpleType )
                {
                    format = CodecUtil.getXSDFormat( element, elementName );
                    elementType = type.getName();

                    elementValue = xmlElement.getChildText( elementName, xmlElement.getNamespace() );
                    if ( elementValue == null )
                    {
                        XMLOutputter outputter = new XMLOutputter( Format.getPrettyFormat() );
                        LOG.debug( outputter.outputString( xmlElement ) );
                        throw new FormatException( "in xmlDoc, can not find the element: [" + elementName + "]" );
                    }

                    String kind = format.getAttribute( "kind" );
                    ValidateField.validateKind( elementName, kind );
                    kindInt = partTypeMap.get( kind ).intValue();

                    switch ( kindInt )
                    {
                        case 0:
                            ValidateFFormat vFFormat = new ValidateFFormat( element, format );

                            if ( ( "string".equals( elementType ) ) || ( "decimal".equals( elementType ) )
                                || ( "hexBinary".equals( elementType ) ) )
                            {
                                int length = vFFormat.getLen();

                                char blank = vFFormat.getBlank();

                                String align = vFFormat.getAlign();
                                if ( elementType.equals( "hexBinary" ) )
                                {
                                    encodeHexBinary( output, elementValue, length, blank, align, elementName );
                                }
                                else
                                {
                                    encodeNonNumber( output, elementValue, length, blank, align, encodeCharset,
                                        elementName );
                                }
                            }
                            else
                            {
                                if ( !( typeList.contains( elementType ) ) )
                                {
                                    throw new FormatException( "not support type [" + elementType + "]" );
                                }

                                String endian = vFFormat.getEndian();
                                encodeNumber( output, elementValue, endian, elementType );
                            }
                            break;
                        case 1:
                            if ( !( "string".equals( elementType ) ) )
                            {
                                throw new FormatException( "element:[" + element.getName() + "],type is:["
                                    + elementType + "] error! unsupported type!" );
                            }

                            String separator = format.getAttribute( "separator" );
                            if ( ( "".equals( separator ) ) || ( separator == null ) )
                            {
                                throw new FormatException(
                                    "element:["
                                        + element.getName()
                                        + "],element format do not define attribute:[separator] or the attribute have no value" );
                            }

                            String limit = format.getAttribute( "limit" );

                            if ( ( limit != null ) && ( !( "".equals( limit ) ) ) )
                            {
                                try
                                {
                                    if ( limit.equals( separator ) )
                                    {
                                        throw new FormatException( "element:[" + elementName
                                            + "],limit can not equals separator" );
                                    }
                                    output.write( limit.getBytes( encodeCharset ) );
                                    output.write( elementValue.getBytes( encodeCharset ) );
                                    output.write( limit.getBytes( encodeCharset ) );
                                    output.write( separator.getBytes( encodeCharset ) );
                                }
                                catch ( UnsupportedEncodingException e )
                                {
                                    throw new FormatException( e.getMessage(), e );
                                }
                            }
                            else
                            {
                                try
                                {
                                    output.write( elementValue.getBytes( encodeCharset ) );
                                    output.write( separator.getBytes( encodeCharset ) );
                                }
                                catch ( UnsupportedEncodingException e )
                                {
                                    throw new FormatException( e.getMessage(), e );
                                }
                            }
                            break;
                        case 2:
                            ValidateVFormat vVFormat = new ValidateVFormat( element, format );
                            if ( ( "string".equals( elementType ) ) || ( "hexBinary".equals( elementType ) ) )
                            {
                                encodeVFiled( output, vVFormat, elementType, encodeCharset, elementValue, elementName );
                            }
                            else
                            {
                                throw new FormatException( "element:[" + element.getName() + "],type is:["
                                    + elementType + "] error! unsupported type!" );
                            }
                            break;
                    }
                }
                if ( ( type instanceof XmlSchemaComplexType ) && ( element.getSchemaType().getName() != null ) )
                {
                    org.jdom.Element xmlElementChild = xmlElement.getChild( elementName );
                    if ( xmlElementChild == null )
                    {
                        throw new FormatException( "in xmlDoc,can not find the element:[" + elementName + "]" );
                    }

                    encodeMessage( CodecUtil.getElements( element.getSchemaType() ), xmlElementChild, output,
                        encodeCharset, schema );
                }
                else
                {
                    if ( ( !( type instanceof XmlSchemaComplexType ) ) || ( element.getSchemaType().getName() != null ) )
                    {
                        continue;
                    }
                    org.w3c.dom.Element occurs = null;
                    if ( element.getAnnotation() != null )
                    {
                        XmlSchemaObjectCollection annColl = element.getAnnotation().getItems();

                        for ( Iterator it = annColl.getIterator(); it.hasNext(); )
                        {
                            Object o = it.next();
                            if ( o instanceof XmlSchemaAppInfo )
                            {
                                XmlSchemaAppInfo appInfo = (XmlSchemaAppInfo) o;
                                NodeList list = appInfo.getMarkup();
                                occurs = CodecUtil.getChildEleOfAppinfo( list, "occurs" );
                            }
                        }
                    }
                    XmlSchemaType loopComplexType = element.getSchemaType();
                    if ( !( loopComplexType instanceof XmlSchemaComplexType ) )
                    {
                        throw new FormatException( "in xsdDoc,can not find the child element:[complexType]" );
                    }
                    int loopNum = 1;

                    if ( occurs != null )
                    {
                        try
                        {
                            String path = occurs.getAttributes().getNamedItem( "ref" ).getNodeValue();
                            if ( ( "".equals( path ) ) || ( path == null ) )
                            {
                                throw new FormatException( "element:[" + element.getName()
                                    + "],element occurs,do not define the attribute[ref] or it has no value!" );
                            }
                            org.jdom.Element xmlChild = xmlElement.getChild( elementName );
                            if ( xmlChild == null )
                            {
                                throw new FormatException( "in xmlDoc,can not find the child element:[" + elementName
                                    + "] of element:[" + xmlElement + "]!" );
                            }
                            org.jdom.Element repeat_num = null;
                            repeat_num = (org.jdom.Element) XPath.selectSingleNode( xmlChild.getParentElement(), path );
                            if ( repeat_num == null )
                            {
                                throw new FormatException( "in xmlDoc,can not find the repeat_num element by path:["
                                    + path + "]!" );
                            }
                            loopNum =
                                Integer.parseInt( ( repeat_num.getText().equals( "" ) ) ? "0" : repeat_num.getText() );
                        }
                        catch ( Exception e )
                        {
                            throw new FormatException( e.getMessage(), e );
                        }
                    }
                    for ( int i = 0; i < loopNum; ++i )
                    {
                        org.jdom.Element xmlElementChild =
                            (org.jdom.Element) xmlElement.getChildren( elementName ).get( i );
                        if ( xmlElementChild == null )
                        {
                            throw new FormatException( "in xmlDoc,can not find the " + ( i + 1 ) + " childElement:["
                                + elementName + "] of element:[" + xmlElement + "]!" );
                        }
                        encodeMessage( CodecUtil.getElements( loopComplexType ), xmlElementChild, output,
                            encodeCharset, schema );
                    }
                }
            }
        }
        catch ( FormatException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new FormatException( e.getMessage(), e );
        }
    }

    private void encodeHexBinary( OutputStream output, String elementValue, int length, int blank, String align,
                                  String elementName )
        throws FormatException
    {
        try
        {
            byte[] temp = HexBinary.decode( elementValue );
            if ( temp.length == length )
            {
                output.write( temp );
                return;
            }
            if ( temp.length > length )
            {
                throw new FormatException( "element[" + elementName + "],hexBinary length over-longer!" );
            }

            if ( "L".equals( align ) )
            {
                output.write( temp );
                for ( int i = 0; i < length - temp.length; ++i )
                {
                    output.write( (byte) blank );
                }
                return;
            }

            for ( int i = 0; i < length - temp.length; ++i )
            {
                output.write( blank );
            }
            output.write( temp );
        }
        catch ( FormatException fe )
        {
            throw fe;
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    private void encodeNumber( OutputStream output, String elementValue, String endian, String elementType )
        throws Exception
    {
        if ( elementType.equals( "int" ) )
        {
            int len = 4;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            setByteOrder( endian, buffer );
            buffer.putInt( Integer.parseInt( elementValue ) );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "unsignedInt" ) )
        {
            int len = 4;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            Long temp = new Long( Long.parseLong( elementValue ) );
            int intValue = temp.intValue();
            setByteOrder( endian, buffer );
            buffer.putInt( intValue );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "long" ) )
        {
            int len = 8;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            setByteOrder( endian, buffer );
            buffer.putLong( Long.parseLong( elementValue ) );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "unsignedLong" ) )
        {
            int len = 8;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            BigInteger tempValue = new BigInteger( elementValue );
            long longValue = tempValue.longValue();
            setByteOrder( endian, buffer );
            buffer.putLong( longValue );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "float" ) )
        {
            int len = 4;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            setByteOrder( endian, buffer );
            buffer.putFloat( Float.parseFloat( elementValue ) );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "double" ) )
        {
            int len = 8;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            setByteOrder( endian, buffer );
            buffer.putDouble( Double.parseDouble( elementValue ) );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "short" ) )
        {
            int len = 2;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            setByteOrder( endian, buffer );
            buffer.putShort( Short.parseShort( elementValue ) );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "unsignedShort" ) )
        {
            int len = 2;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            Integer temp = new Integer( Integer.parseInt( elementValue ) );
            short shortValue = temp.shortValue();
            setByteOrder( endian, buffer );
            buffer.putShort( shortValue );
            output.write( buffer.array() );
        }
        else if ( elementType.equals( "byte" ) )
        {
            int len = 1;
            ByteBuffer buffer = ByteBuffer.allocate( len );
            setByteOrder( endian, buffer );
            byte val = (byte) Integer.parseInt( elementValue );
            buffer.put( val );
            output.write( buffer.array() );
        }
    }

    private void setByteOrder( String endian, ByteBuffer buf )
    {
        if ( "big".equals( endian ) )
        {
            buf.order( ByteOrder.BIG_ENDIAN );
        }
        else
        {
            buf.order( ByteOrder.LITTLE_ENDIAN );
        }
    }

    public void encodeNonNumber( OutputStream output, String elementValue, int length, char blank, String align,
                                 String encodeCharset, String elementName )
        throws FormatException
    {
        String chgValue = elementValue;
        chgValue = RidFillBlank.fillBlank( elementValue, length, blank, align, encodeCharset, elementName );
        try
        {
            output.write( chgValue.getBytes( encodeCharset ) );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public void encodeVFiled( OutputStream output, ValidateVFormat vVFormat, String elementType, String encodeCharset,
                              String elementValue, String elementName )
        throws FormatException
    {
        int headLength = vVFormat.getHeadLength();

        char headBlank = vVFormat.getHeadBlank();

        String headAlign = vVFormat.getHeadAlign();

        int headRadix = vVFormat.getHeadRadix();
        try
        {
            String headValue = null;

            if ( "string".equals( elementType ) )
            {
                headValue = Integer.toString( elementValue.getBytes( encodeCharset ).length, headRadix );

                if ( headRadix == 2 )
                {
                    headValue = Integer.toString( elementValue.getBytes( encodeCharset ).length, 16 );

                    if ( headValue.length() % 2 != 0 )
                    {
                        headValue = "0" + headValue;
                    }
                    headValue = new String( HexBinary.decode( headValue ) );
                }
            }
            else
            {
                if ( elementValue.getBytes( encodeCharset ).length % 2 != 0 )
                {
                    throw new FormatException( "element:[" + elementName + "],type:[" + elementType
                        + "],value length is:[" + elementValue.getBytes( encodeCharset ).length
                        + " ]error! it length must can divide by 2" );
                }

                headValue = Integer.toString( elementValue.getBytes( encodeCharset ).length / 2, headRadix );
                if ( headRadix == 2 )
                {
                    headValue = Integer.toString( elementValue.getBytes( encodeCharset ).length / 2, 16 );

                    if ( headValue.length() % 2 != 0 )
                    {
                        headValue = "0" + headValue;
                    }
                    headValue = new String( HexBinary.decode( headValue ) );
                }
            }
            encodeHead( output, headValue, headLength, headBlank, headAlign, headRadix, encodeCharset, elementName );

            if ( "string".equals( elementType ) )
            {
                output.write( elementValue.getBytes( encodeCharset ) );
                return;
            }
            output.write( HexBinary.decode( elementValue ) );
        }
        catch ( IOException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public void encodeHead( OutputStream output, String headValue, int headLength, char headBlank, String headAlign,
                            int headRadix, String encodeCharset, String elementName )
        throws FormatException
    {
        String chgValue = headValue;
        chgValue = RidFillBlank.fillBlank( headValue, headLength, headBlank, headAlign, encodeCharset, elementName );
        try
        {
            output.write( chgValue.getBytes( encodeCharset ) );
        }
        catch ( IOException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void init( String workspaceRoot )
    {
    }

    public long encode( Source input, XmlSchema schema, String charset, OutputStream output )
        throws FormatException
    {
        Document message = JdomUtil.source2JDOMDocument( input );
        String elementName = message.getRootElement().getName();

        if ( charset == null )
        {
            throw new FormatException( "charset not specified" );
        }
        if ( schema == null )
        {
            throw new FormatException( " function encode,schema is null" );
        }
        if ( message == null )
        {
            ;
        }
        message.getRootElement().setName( elementName );
        try
        {
            encode( message.getRootElement(), schema, output, charset );
            return 0L;
        }
        catch ( FormatException fe )
        {
            throw fe;
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }
}