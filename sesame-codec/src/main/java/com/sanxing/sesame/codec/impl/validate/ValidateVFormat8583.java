package com.sanxing.sesame.codec.impl.validate;

import org.jdom.Namespace;

import com.sanxing.sesame.binding.codec.FormatException;

public class ValidateVFormat8583
{
    private int headLen;

    private char headBlank = ' ';

    private String headAlign;

    private int headCompress = 0;

    private int compress = 0;

    private final int headRadix = 10;

    private int id = 0;

    public ValidateVFormat8583( org.jdom.Element element, org.jdom.Element format, Namespace xsd )
        throws FormatException
    {
        String elementName = element.getAttributeValue( "name" );

        id = Integer.parseInt( format.getAttributeValue( "id" ) );
        org.jdom.Element head = format.getChild( "head", xsd );
        if ( head == null )
        {
            throw new FormatException( "element:[" + elementName
                + "],element format,do not define child element[head]!" );
        }

        String headLenStr = head.getAttributeValue( "length" );
        headLen = ValidateField.validateLength( elementName, headLenStr );

        String headCompressStr = head.getAttributeValue( "compress" );
        headCompress = ValidateField.validateCompress( elementName, headCompressStr );

        String blankStr = head.getAttributeValue( "blank" );
        headBlank = ValidateField.validateBlank( elementName, headBlank, blankStr );

        headAlign = head.getAttributeValue( "align" );
        headAlign = ValidateField.validateAlign( elementName, headAlign );

        int headRadix = 0;
        headRadix = ValidateField.validateRadix( head.getAttributeValue( "radix" ), headRadix );

        if ( !( "string".equals( element.getAttributeValue( "type" ) ) ) )
        {
            return;
        }
        compress = ValidateField.validateCompress( elementName, format.getAttributeValue( "compress" ) );
    }

    public ValidateVFormat8583( String elementName, String type, org.jdom.Element format, Namespace xsd )
        throws FormatException
    {
        id = Integer.parseInt( format.getAttributeValue( "id" ) );
        org.jdom.Element head = format.getChild( "head", xsd );
        if ( head == null )
        {
            throw new FormatException( "element:[" + elementName
                + "],element format,do not define child element[head]!" );
        }

        String headLenStr = head.getAttributeValue( "length" );
        headLen = ValidateField.validateLength( elementName, headLenStr );

        String headCompressStr = head.getAttributeValue( "compress" );
        headCompress = ValidateField.validateCompress( elementName, headCompressStr );

        String blankStr = head.getAttributeValue( "blank" );
        headBlank = ValidateField.validateBlank( elementName, headBlank, blankStr );

        headAlign = head.getAttributeValue( "align" );
        headAlign = ValidateField.validateAlign( elementName, headAlign );

        int headRadix = 0;
        headRadix = ValidateField.validateRadix( head.getAttributeValue( "radix" ), headRadix );

        if ( !( "string".equals( type ) ) )
        {
            return;
        }
        compress = ValidateField.validateCompress( elementName, format.getAttributeValue( "compress" ) );
    }

    public ValidateVFormat8583( String elementType, String elementName, org.w3c.dom.Element format )
        throws FormatException
    {
        id = Integer.parseInt( format.getAttribute( "id" ) );
        org.w3c.dom.Element head = (org.w3c.dom.Element) format.getElementsByTagName( "head" ).item( 0 );
        if ( head == null )
        {
            throw new FormatException( "element:[" + elementName
                + "],element format,do not define child element[head]!" );
        }

        String headLenStr = head.getAttribute( "length" );
        headLen = ValidateField.validateLength( elementName, headLenStr );

        String headCompressStr = head.getAttribute( "compress" );
        headCompress = ValidateField.validateCompress( elementName, headCompressStr );

        String blankStr = head.getAttribute( "blank" );
        headBlank = ValidateField.validateBlank( elementName, headBlank, blankStr );

        headAlign = head.getAttribute( "align" );
        headAlign = ValidateField.validateAlign( elementName, headAlign );

        int headRadix = 0;
        headRadix = ValidateField.validateRadix( head.getAttribute( "radix" ), headRadix );

        if ( !( "string".equals( elementType ) ) )
        {
            return;
        }
        compress = ValidateField.validateCompress( elementName, format.getAttribute( "compress" ) );
    }

    public int getCompress()
    {
        return compress;
    }

    public String getHeadAlign()
    {
        return headAlign;
    }

    public char getHeadBlank()
    {
        return headBlank;
    }

    public int getHeadCompress()
    {
        return headCompress;
    }

    public int getHeadLen()
    {
        return headLen;
    }

    public int getHeadRadix()
    {
        return headRadix;
    }

    public int getId()
    {
        return id;
    }
}