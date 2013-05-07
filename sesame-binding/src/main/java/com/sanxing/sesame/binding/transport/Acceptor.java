package com.sanxing.sesame.binding.transport;

import java.io.IOException;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;

public abstract interface Acceptor
    extends Transport
{
    public abstract void reply( MessageContext paramMessageContext )
        throws BindingException, IOException;
}