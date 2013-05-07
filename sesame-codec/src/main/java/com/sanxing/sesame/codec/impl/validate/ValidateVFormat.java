package com.sanxing.sesame.codec.impl.validate;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.w3c.dom.Element;

import com.sanxing.sesame.binding.codec.FormatException;

public class ValidateVFormat
{
    private final int headLength;

    private char headBlank = ' ';

    private String headAlign;

    private int headRadix = 10;

    private int id = 0;

    public ValidateVFormat( XmlSchemaElement element, Element format )
        throws FormatException
    {
        Element head = (Element) format.getElementsByTagName( "head" ).item( 0 );
        if ( head == null )
        {
            throw new FormatException( "element:[" + element.getName()
                + "],element format,do not define child element[head]!" );
        }

        String headLengthStr = head.getAttribute( "length" );
        headLength = ValidateField.validateLength( element.getName(), headLengthStr );

        String headBlankStr = head.getAttribute( "blank" );
        headBlank = ValidateField.validateBlank( element.getName(), headBlank, headBlankStr );

        headAlign = head.getAttribute( "align" );
        headAlign = ValidateField.validateAlign( element.getName(), headAlign );

        headRadix = ValidateField.validateRadix( head.getAttribute( "radix" ), headRadix );

        String idStr = format.getAttribute( "id" );
        if ( !"".equals( idStr ) )
        {
            id = Integer.parseInt( idStr );
        }
    }

    public String getHeadAlign()
    {
        return headAlign;
    }

    public char getHeadBlank()
    {
        return headBlank;
    }

    public int getHeadLength()
    {
        return headLength;
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