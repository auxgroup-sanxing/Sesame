package com.sanxing.sesame.codec.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Iterator;

import javax.xml.transform.Source;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom.Document;
import org.jdom.transform.JDOMSource;

import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.codec.impl.validate.ValidateFFormat8583;
import com.sanxing.sesame.codec.impl.validate.ValidateField;
import com.sanxing.sesame.codec.impl.validate.ValidateVFormat8583;
import com.sanxing.sesame.codec.util.BCD;
import com.sanxing.sesame.codec.util.BitMap;
import com.sanxing.sesame.codec.util.CodecUtil;
import com.sanxing.sesame.codec.util.HexBinary;
import com.sanxing.sesame.codec.util.RidFillBlank;

public class Decode8583
    implements Decoder
{
    public org.jdom.Element decode( byte[] message, int length, String charset, XmlSchemaElement schemaElement )
        throws FormatException
    {
        if ( length > message.length )
        {
            throw new FormatException( "Decode8583 ,parameter length value bigger then real buffer length error!" );
        }
        ByteBuffer msgBuf = ByteBuffer.wrap( message );

        msgBuf.limit( length );
        org.jdom.Element root = null;
        try
        {
            if ( schemaElement == null )
            {
                throw new FormatException( "Decode8583,parameter  [XmlSchemaElement] is null!" );
            }
            XmlSchemaType xsdType = schemaElement.getSchemaType();
            if ( !( xsdType instanceof XmlSchemaComplexType ) )
            {
                throw new FormatException( "in xsdDoc,can not find the child element:[complexType]" );
            }
            root = new org.jdom.Element( schemaElement.getName() );
            decodeMessage( msgBuf, xsdType, charset, root );
            if ( msgBuf.position() != msgBuf.limit() )
            {
                throw new FormatException( "parameter [length] bigger then real need buffer length" );
            }
        }
        catch ( FormatException fe )
        {
            throw fe;
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }

        return root;
    }

    protected void decodeMessage( ByteBuffer recvBuf, XmlSchemaType schemaType, String charset, org.jdom.Element root )
        throws FormatException
    {
        try
        {
            BitMap bitMap = new BitMap();
            BitSet bs = BitMap.getBitset( recvBuf );

            BitSet tempBs = new BitSet();

            Iterator elements = CodecUtil.getElements( schemaType );
            while ( elements.hasNext() )
            {
                XmlSchemaElement element = (XmlSchemaElement) elements.next();

                String elementName = element.getName();

                org.jdom.Element childOfElementMessage = new org.jdom.Element( elementName );

                String elementType = element.getSchemaType().getName();

                org.w3c.dom.Element format = CodecUtil.getXSDFormat( element, elementName );

                int id = ValidateField.validateEId( format.getAttribute( "id" ), elementName );

                if ( !( bs.get( id - 1 ) ) )
                {
                    root.addContent( childOfElementMessage );
                }
                else
                {
                    tempBs.set( id - 1 );

                    String kind = format.getAttribute( "kind" );
                    ValidateField.validate8583Kind( elementName, kind );

                    if ( "F".equals( kind ) )
                    {
                        ValidateFFormat8583 vFFormat = new ValidateFFormat8583( elementType, elementName, format );
                        if ( "string".equals( elementType ) )
                        {
                            int len = vFFormat.getLen();

                            String align = vFFormat.getAlign();

                            int blank = vFFormat.getBlank();

                            int compress = vFFormat.getCompress();

                            String eleValue = getFField( recvBuf, len, blank, compress, align, charset, elementType );
                            childOfElementMessage.addContent( eleValue );
                        }
                        else if ( "int".equals( elementType ) )
                        {
                            String endian = vFFormat.getEndian();

                            int len = 4;
                            byte[] temp = new byte[len];
                            recvBuf.get( temp );
                            ByteBuffer buf = ByteBuffer.allocate( len );
                            CodecUtil.setByteOrder( endian, buf );
                            buf.put( temp );
                            buf.flip();

                            childOfElementMessage.addContent( "" + buf.getInt() );
                        }
                        else if ( "hexBinary".equals( elementType ) )
                        {
                            int len = vFFormat.getLen();

                            byte[] temp = new byte[len];
                            recvBuf.get( temp );
                            childOfElementMessage.addContent( HexBinary.encode( temp ) );
                        }
                    }
                    else if ( "V".equals( kind ) )
                    {
                        ValidateVFormat8583 vVFormat = new ValidateVFormat8583( elementType, elementName, format );

                        int headLen = vVFormat.getHeadLen();

                        int headCompress = vVFormat.getHeadCompress();

                        int headBlank = vVFormat.getHeadBlank();

                        String headAlign = vVFormat.getHeadAlign();

                        int headRadix = vVFormat.getHeadRadix();

                        int compress = vVFormat.getCompress();

                        String eleValue =
                            getVField( recvBuf, headLen, headBlank, compress, headAlign, headCompress, headRadix,
                                elementType, charset, elementName );
                        childOfElementMessage.addContent( eleValue );
                    }
                    else
                    {
                        throw new FormatException( "element format,attribute kind value is:[" + kind + "]error" );
                    }
                    root.addContent( childOfElementMessage );
                }
            }
            if ( tempBs.length() > 64 )
            {
                tempBs.set( 0 );
            }
            if ( !( tempBs.equals( bs ) ) )
            {
                throw new FormatException( "bit map error! bitBuffer is not match with schema!" );
            }
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

    public String getFField( ByteBuffer buf, int len, int blank, int compress, String align, String charset,
                             String elementType )
        throws FormatException
    {
        String result = null;
        byte[] temp = null;
        try
        {
            temp = getBinaryBuf( len, compress );
            buf.get( temp );
            if ( 1 == compress )
            {
                temp = BCD.bcd2str( temp, len, align ).getBytes( charset );
            }
            if ( "L".equals( align ) )
            {
                result = RidFillBlank.ridRightBlank( temp, blank, elementType, charset );
            }
            else
            {
                result = RidFillBlank.ridLeftBlank( temp, blank, elementType, charset );
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
        return result;
    }

    private byte[] getBinaryBuf( int len, int compress )
    {
        byte[] temp;
        if ( 1 == compress )
        {
            if ( len % 2 == 0 )
            {
                temp = new byte[len / 2];
            }
            else
            {
                temp = new byte[len / 2 + 1];
            }
        }
        else
        {
            temp = new byte[len];
        }
        return temp;
    }

    public String getVField( ByteBuffer buf, int headLen, int headBlank, int compress, String headAlign,
                             int headCompress, int headRadix, String elementType, String charset, String elementName )
        throws FormatException
    {
        String result = null;
        try
        {
            String realHeadValue = getHeadValue( buf, headLen, headBlank, headCompress, headAlign, headRadix, charset );

            int eleLen = 0;
            if ( realHeadValue.length() > 0 )
            {
                if ( headRadix != 2 )
                {
                    eleLen = Integer.parseInt( realHeadValue, headRadix );
                }
                else
                {
                    eleLen = Integer.parseInt( realHeadValue, 16 );
                }

            }

            result = getEleValue( buf, compress, headAlign, elementType, charset, result, eleLen, elementName );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
        return ( ( result == null ) ? "" : result );
    }

    private String getHeadValue( ByteBuffer buf, int headLen, int headBlank, int headCompress, String headAlign,
                                 int headRadix, String charset )
        throws FormatException
    {
        String result = null;
        byte[] temp = null;
        try
        {
            temp = getBinaryBuf( headLen, headCompress );
            buf.get( temp );
            String elementType = null;
            if ( headRadix != 2 )
            {
                elementType = "string";
            }
            else
            {
                elementType = "hexBinary";
            }
            if ( 1 == headCompress )
            {
                temp = BCD.bcd2str( temp, headLen, headAlign ).getBytes( charset );
            }
            if ( "L".equals( headAlign ) )
            {
                result = RidFillBlank.ridRightBlank( temp, headBlank, elementType, charset );
            }
            else
            {
                result = RidFillBlank.ridLeftBlank( temp, headBlank, elementType, charset );
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
        return result;
    }

    private String getEleValue( ByteBuffer buf, int compress, String align, String elementType, String charset,
                                String result, int eleLen, String elementName )
        throws UnsupportedEncodingException, FormatException
    {
        if ( "string".equals( elementType ) )
        {
            byte[] eleBuf = getBinaryBuf( eleLen, compress );
            buf.get( eleBuf );
            if ( 1 == compress )
            {
                result = BCD.bcd2str( eleBuf, eleLen, align );
            }
            else
            {
                result = new String( eleBuf, charset );
            }
        }
        else if ( "hexBinary".equals( elementType ) )
        {
            byte[] temp = new byte[eleLen];
            buf.get( temp );
            result = HexBinary.encode( temp );
        }
        else
        {
            throw new FormatException( "element:[" + elementName + "],kind is:[V],type is:[" + elementType + "]error!" );
        }
        return result;
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void init( String workspaceRoot )
    {
    }

    @Override
    public void decode( BinarySource inputSource, XMLResult outputResult )
        throws FormatException
    {
        XmlSchema schema = inputSource.getXMLSchema();

        InputStream stream = inputSource.getInputStream();
        String charset = inputSource.getEncoding();
        if ( schema == null )
        {
            throw new FormatException( "in Decode8583,can not get xmlschema !" );
        }
        XmlSchemaElement schemaEl = inputSource.getRootElement();
        try
        {
            int length = stream.available();
            byte[] bytes = new byte[length];
            stream.read( bytes );
            org.jdom.Element element = decode( bytes, length, charset, schemaEl );
            element.detach();

            outputResult.setDocument( new Document( element ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            throw new FormatException( e.getMessage(), e );
        }
    }

    public Source decode( InputStream input, XmlSchema schema, String rootName, String charset )
        throws FormatException
    {
        if ( schema == null )
        {
            throw new FormatException( "in Decode8583,can not get xmlschema !" );
        }
        XmlSchemaElement schemaEl = schema.getElementByName( rootName );
        try
        {
            int length = input.available();
            byte[] bytes = new byte[length];
            input.read( bytes );
            org.jdom.Element element = decode( bytes, length, charset, schemaEl );
            element.detach();

            return new JDOMSource( new Document( element ) );
        }
        catch ( IOException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }
}