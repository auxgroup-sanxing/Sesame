package com.sanxing.sesame.binding.codec;

import com.sanxing.sesame.binding.context.MessageContext;

public abstract interface FaultHandler
{
    public abstract void decode( BinarySource paramBinarySource, XMLResult paramXMLResult );

    public abstract void encode( XMLSource paramXMLSource, BinaryResult paramBinaryResult );

    public abstract void handle( Exception paramException, MessageContext paramMessageContext );
}