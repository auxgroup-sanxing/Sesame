package com.sanxing.sesame.binding.transport;

import java.io.IOException;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;

public abstract interface Connector
    extends Transport
{
    public abstract void sendOut( MessageContext paramMessageContext )
        throws BindingException, IOException;
}