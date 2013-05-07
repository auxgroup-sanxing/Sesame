package com.sanxing.sesame.codec.impl.validate;

import org.jdom.DataConversionException;

import com.sanxing.sesame.binding.codec.FormatException;

public class ValidateField
{
    public static String validateAlign( String elementName, String align )
        throws FormatException
    {
        if ( ( align == null ) || ( "".equals( align ) ) )
        {
            return "L";
        }
        if ( ( !( "L".equals( align ) ) ) && ( !( "R".equals( align ) ) ) )
        {
            throw new FormatException( "element:[" + elementName
                + "],element format attribute [align] or child element head attribute [align], value is:[" + align
                + "]error!" );
        }
        return align;
    }

    public static int validateEId( String idStr, String elementName )
        throws FormatException, DataConversionException
    {
        if ( ( "".equals( idStr ) ) || ( idStr == null ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName
                + "] ,format hava not define the attribute [id] or it has no value!" );
        }
        int id = Integer.parseInt( idStr );
        if ( ( id < 2 ) || ( id > 128 ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName + "] , format,attribute [id],value:[" + id
                + "]error!" );
        }
        return id;
    }

    public static int validateSimplarity8583EId( String idStr, String elementName )
        throws FormatException, DataConversionException
    {
        if ( ( "".equals( idStr ) ) || ( idStr == null ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName
                + "] ,format hava not define the attribute [id] or it has no value!" );
        }
        int id = Integer.parseInt( idStr );
        if ( ( id < 1 ) || ( id > 128 ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName + "] , format,attribute [id],value:[" + id
                + "]error!" );
        }
        return id;
    }

    public static void validate8583Kind( String elementName, String kind )
        throws FormatException
    {
        if ( ( "".equals( kind ) ) || ( kind == null ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName
                + "], element format,the attribute [kind] do not define or it have no value!" );
        }
        if ( ( !( "F".equals( kind ) ) ) && ( !( "V".equals( kind ) ) ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName
                + "],element format,the attribute [kind] value is:[" + kind + "] error!" );
        }
    }

    public static char validateBlank( String elementName, char blank, String blankStr )
        throws FormatException
    {
        if ( ( !( "".equals( blankStr ) ) ) && ( blankStr != null ) )
        {
            if ( blankStr.getBytes().length > 1 )
            {
                if ( blankStr.equals( "\\0" ) )
                {
                    blank = '\0';
                }
                throw new FormatException( "in xsdDoc,element:[" + elementName
                    + "],element format attribute [blank] or child element head attribute [blank], value:[" + blankStr
                    + "] error! blank value must be only one byte!" );
            }
            else
            {
                blank = blankStr.charAt( 0 );
            }

        }

        return blank;
    }

    public static int validateRadix( String headRadixStr, int headRadix )
    {
        if ( ( !( "".equals( headRadixStr ) ) ) && ( headRadixStr != null ) )
        {
            headRadix = Integer.parseInt( headRadixStr );
        }
        return headRadix;
    }

    public static int validateCompress( String elementName, String compressStr )
        throws FormatException
    {
        if ( ( "".equals( compressStr ) ) || ( compressStr == null ) )
        {
            throw new FormatException(
                "in xsdDoc,element:["
                    + elementName
                    + "], element format,the attribute [compress] or child element head attribute [compress], do not define or it have no value!" );
        }
        int compress = Integer.parseInt( compressStr );
        if ( ( compress != 0 ) && ( compress != 1 ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName
                + "], element format,the attribute [compress] or child element head attribute [compress],value is:["
                + compress + "]error!" );
        }
        return compress;
    }

    public static int validateLength( String elementName, String lenStr )
        throws FormatException
    {
        if ( ( "".equals( lenStr ) ) || ( lenStr == null ) )
        {
            throw new FormatException(
                "in xsdDoc,element:["
                    + elementName
                    + "], element format attribute [length] or child element head attribute [Length], do not define or it have no value!" );
        }
        int len = Integer.parseInt( lenStr );
        if ( len == 0 )
        {
            throw new FormatException(
                "in xsdDoc,element:["
                    + elementName
                    + "],element format,attribute [length] or child element head attribute [Length],  int value is 0 error!" );
        }
        return len;
    }

    public static String validateEndian( String elementName, String endian )
        throws FormatException
    {
        if ( ( endian == null ) || ( "".equals( endian ) ) )
        {
            return "big";
        }
        if ( ( !( "big".equals( endian ) ) ) && ( !( "little".equals( endian ) ) ) )
        {
            throw new FormatException( "element:[" + elementName + "],endian:[" + endian + "] error!" );
        }
        return endian;
    }

    public static void validateKind( String elementName, String kind )
        throws FormatException
    {
        if ( ( "".equals( kind ) ) || ( kind == null ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName
                + "], element format,the attribute [kind] do not define or it have no value!" );
        }
        if ( ( !( "F".equals( kind ) ) ) && ( !( "S".equals( kind ) ) ) && ( !( "V".equals( kind ) ) ) )
        {
            throw new FormatException( "in xsdDoc,element:[" + elementName
                + "],element format,the attribute [kind] value is:[" + kind + "] error!" );
        }
    }
}