package com.sanxing.sesame.binding.codec;

public abstract interface Decoder
{
    public abstract void init( String paramString );

    public abstract void decode( BinarySource paramBinarySource, XMLResult paramXMLResult )
        throws FormatException;

    public abstract void destroy();
}