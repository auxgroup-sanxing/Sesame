package com.sanxing.sesame.binding.codec;

public interface Decoder
{
    public abstract void init( String paramString );

    public abstract void decode( BinarySource source, XMLResult result )
        throws FormatException;

    public abstract void destroy();
}