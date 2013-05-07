package com.sanxing.sesame.codec.impl.validate;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.w3c.dom.Element;

import com.sanxing.sesame.binding.codec.FormatException;

public class ValidateFFormat
{
    private int len;

    private char blank = ' ';

    private String align;

    private String endian;

    public ValidateFFormat( XmlSchemaElement element, Element format )
        throws FormatException
    {
        String type = element.getSchemaType().getName();
        if ( ( "string".equals( type ) ) || ( "decimal".equals( type ) ) || ( "hexBinary".equals( type ) ) )
        {
            String lenStr = format.getAttribute( "length" );
            len = ValidateField.validateLength( element.getName(), lenStr );

            String blankStr = format.getAttribute( "blank" );
            blank = ValidateField.validateBlank( element.getName(), blank, blankStr );

            align = format.getAttribute( "align" );
            align = ValidateField.validateAlign( element.getName(), align );
        }
        else
        {
            endian = format.getAttribute( "endian" );
            endian = ValidateField.validateEndian( element.getName(), endian );
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

    public int getLen()
    {
        return len;
    }

    public String getEndian()
    {
        return endian;
    }
}