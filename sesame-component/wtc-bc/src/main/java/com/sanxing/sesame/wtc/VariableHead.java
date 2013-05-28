package com.sanxing.sesame.wtc;

public class VariableHead
{
    public static final int BINARY = 2;

    public static final int OCTAL = 8;

    public static final int DECIMAL = 10;

    public static final int HEX = 16;

    private int headLength;

    private char headBlank;

    private String headAlign;

    private int headRadix;

    private int id;

    public int getHeadLength()
    {
        return this.headLength;
    }

    public void setHeadLength( int headLength )
    {
        this.headLength = headLength;
    }

    public char getHeadBlank()
    {
        return this.headBlank;
    }

    public void setHeadBlank( char headBlank )
    {
        this.headBlank = headBlank;
    }

    public String getHeadAlign()
    {
        return this.headAlign;
    }

    public void setHeadAlign( String headAlign )
    {
        this.headAlign = headAlign;
    }

    public int getHeadRadix()
    {
        return this.headRadix;
    }

    public void setHeadRadix( int headRadix )
    {
        this.headRadix = headRadix;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId( int id )
    {
        this.id = id;
    }
}
