package com.sanxing.sesame.binding.codec;

public interface Encoder
{
    public abstract void init( String paramString );

    public abstract void encode( XMLSource source, BinaryResult result )
        throws FormatException;

    public abstract void destroy();
}