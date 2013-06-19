package com.sanxing.sesame.binding.codec;

import com.sanxing.sesame.binding.context.MessageContext;

public interface FaultHandler
{
    public abstract void decode( BinarySource source, XMLResult result );

    public abstract void encode( XMLSource source, BinaryResult result );

    public abstract void handle( Exception exception, MessageContext context );
}