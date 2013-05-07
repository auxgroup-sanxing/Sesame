package com.sanxing.sesame.binding.codec;

public abstract interface Encoder
{
    public abstract void init( String paramString );

    public abstract void encode( XMLSource paramXMLSource, BinaryResult paramBinaryResult )
        throws FormatException;

    public abstract void destroy();
}