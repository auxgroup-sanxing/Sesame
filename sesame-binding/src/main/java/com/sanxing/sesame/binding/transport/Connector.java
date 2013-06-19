package com.sanxing.sesame.binding.transport;

import java.io.IOException;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;

public interface Connector
    extends Transport
{
    public abstract void sendOut( MessageContext context )
        throws BindingException, IOException;
}