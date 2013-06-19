package com.sanxing.sesame.binding.transport;

import java.io.IOException;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;

public interface Acceptor
    extends Transport
{
    public abstract void reply( MessageContext context )
        throws BindingException, IOException;
}