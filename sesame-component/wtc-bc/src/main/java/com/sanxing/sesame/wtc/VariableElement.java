package com.sanxing.sesame.wtc;

import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.codec.util.HexBinary;
import java.io.IOException;

public class VariableElement
{
    private VariableHead head;

    private String elementType;

    private String elementName;

    private String elementValue;

    private String encodeCharset;

    public VariableHead getHead()
    {
        return this.head;
    }

    public void setHead( VariableHead head )
    {
        this.head = head;
    }

    public String getElementType()
    {
        return this.elementType;
    }

    public void setElementType( String elementType )
    {
        this.elementType = elementType;
    }

    public String getElementName()
    {
        return this.elementName;
    }

    public void setElementName( String elementName )
    {
        this.elementName = elementName;
    }

    public String getElementValue()
    {
        return this.elementValue;
    }

    public void setElementValue( String elementValue )
    {
        this.elementValue = elementValue;
    }

    public String getEncodeCharset()
    {
        return this.encodeCharset;
    }

    public void setEncodeCharset( String encodeCharset )
    {
        this.encodeCharset = encodeCharset;
    }

    public int getByteNumber()
        throws FormatException
    {
        try
        {
            int length = 0;

            if ( "string".equals( this.elementType ) )
            {
                length = this.elementValue.getBytes( this.encodeCharset ).length;
            }
            else if ( this.elementValue.getBytes( this.encodeCharset ).length % 2 != 0 )
            {
                throw new FormatException( "element:[" + this.elementName + "],type:[" + this.elementType
                    + "],value length is:[" + this.elementValue.getBytes( this.encodeCharset ).length
                    + " ]error! it length must can divide by 2" );
            }

            return this.elementValue.getBytes( this.encodeCharset ).length / 2;
        }
        catch ( IOException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public byte[] getHeadBytes()
        throws FormatException
    {
        try
        {
            int headRadix = this.head.getHeadRadix();
            int length = getByteNumber();
            if ( length >= 250 )
            {
                length = 255;
            }
            String headValue = Integer.toString( length, headRadix );
            byte[] bytes = headValue.getBytes( this.encodeCharset );
            if ( headRadix == 2 )
            {
                headValue = Integer.toString( length, 16 );

                if ( headValue.length() % 2 != 0 )
                    headValue = "0" + headValue;
            }
            return HexBinary.decode( headValue );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public String subString( int start, int number )
        throws FormatException
    {
        try
        {
            int total = getByteNumber();
            if ( ( start < 0 ) || ( number + start > total ) )
            {
                throw new FormatException( "subString fail, total length is " + total );
            }

            byte[] src = this.elementValue.getBytes( this.encodeCharset );
            byte[] dest = new byte[number];
            System.arraycopy( src, start, dest, 0, number );

            return new String( dest, this.encodeCharset );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public String subString( int start )
        throws FormatException
    {
        try
        {
            int total = getByteNumber();
            if ( ( start < 0 ) || ( start >= total ) )
            {
                throw new FormatException( "subString fail, total length is " + total );
            }
            int number = total - start;
            return subString( start, number );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }
}
