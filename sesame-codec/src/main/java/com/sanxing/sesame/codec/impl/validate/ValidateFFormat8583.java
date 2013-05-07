package com.sanxing.sesame.codec.impl.validate;

import com.sanxing.sesame.binding.codec.FormatException;

public class ValidateFFormat8583
{
    private int len;

    private char blank = ' ';

    private String align;

    private String endian;

    private int compress = 0;

    private int id = 0;

    public ValidateFFormat8583( org.jdom.Element element, org.jdom.Element format )
        throws FormatException
    {
        String type = element.getAttributeValue( "type" );
        id = Integer.parseInt( format.getAttributeValue( "id" ) );
        if ( "string".equals( type ) )
        {
            String lenStr = format.getAttributeValue( "length" );
            len = ValidateField.validateLength( element.getAttributeValue( "name" ), lenStr );

            align = format.getAttributeValue( "align", "L" );
            align = ValidateField.validateAlign( element.getAttributeValue( "name" ), align );

            String blankStr = format.getAttributeValue( "blank" );
            blank = ValidateField.validateBlank( element.getAttributeValue( "name" ), blank, blankStr );

            String compressStr = format.getAttributeValue( "compress" );
            compress = ValidateField.validateCompress( element.getAttributeValue( "name" ), compressStr );
        }
        else if ( "int".equals( type ) )
        {
            endian = format.getAttributeValue( "endian", "big" );
            endian = ValidateField.validateEndian( element.getAttributeValue( "name" ), endian );
        }
        else if ( "hexBinary".equals( type ) )
        {
            String lenStr = format.getAttributeValue( "length" );
            len = ValidateField.validateLength( element.getAttributeValue( "name" ), lenStr );
        }
        else
        {
            throw new FormatException( "element:[" + element.getAttributeValue( "name" ) + "],attribute type is:["
                + type + "] error!" );
        }
    }

    public ValidateFFormat8583( String elementName, org.jdom.Element format, String type )
        throws FormatException
    {
        id = Integer.parseInt( format.getAttributeValue( "id" ) );
        if ( "string".equals( type ) )
        {
            String lenStr = format.getAttributeValue( "length" );
            len = ValidateField.validateLength( elementName, lenStr );

            align = format.getAttributeValue( "align", "L" );
            align = ValidateField.validateAlign( elementName, align );

            String blankStr = format.getAttributeValue( "blank" );
            blank = ValidateField.validateBlank( elementName, blank, blankStr );

            String compressStr = format.getAttributeValue( "compress" );
            compress = ValidateField.validateCompress( elementName, compressStr );
        }
        else if ( "int".equals( type ) )
        {
            endian = format.getAttributeValue( "endian", "big" );
            endian = ValidateField.validateEndian( elementName, endian );
        }
        else if ( "hexBinary".equals( type ) )
        {
            String lenStr = format.getAttributeValue( "length" );
            len = ValidateField.validateLength( elementName, lenStr );
        }
        else
        {
            throw new FormatException( "element:[" + elementName + "],attribute type is:[" + type + "] error!" );
        }
    }

    public ValidateFFormat8583( String elementType, String elementName, org.w3c.dom.Element format )
        throws FormatException
    {
        id = Integer.parseInt( format.getAttribute( "id" ) );
        if ( "string".equals( elementType ) )
        {
            String lenStr = format.getAttribute( "length" );
            len = ValidateField.validateLength( elementName, lenStr );

            align = format.getAttribute( "align" );
            align = ValidateField.validateAlign( elementName, align );

            String blankStr = format.getAttribute( "blank" );
            blank = ValidateField.validateBlank( elementName, blank, blankStr );

            String compressStr = format.getAttribute( "compress" );
            compress = ValidateField.validateCompress( elementName, compressStr );
        }
        else if ( "int".equals( elementType ) )
        {
            endian = format.getAttribute( "endian" );
            endian = ValidateField.validateEndian( elementName, endian );
        }
        else if ( "hexBinary".equals( elementType ) )
        {
            String lenStr = format.getAttribute( "length" );
            len = ValidateField.validateLength( elementName, lenStr );
        }
        else
        {
            throw new FormatException( "element:[" + elementName + "],attribute type is:[" + elementType + "] error!" );
        }
    }

    public String getAlign()
    {
        return align;
    }

    public char getBlank()
    {
        return blank;
    }

    public int getCompress()
    {
        return compress;
    }

    public String getEndian()
    {
        return endian;
    }

    public int getLen()
    {
        return len;
    }

    public int getId()
    {
        return id;
    }
}