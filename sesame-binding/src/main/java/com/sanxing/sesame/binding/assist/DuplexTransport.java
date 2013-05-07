package com.sanxing.sesame.binding.assist;

import java.io.IOException;

import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;

public abstract class DuplexTransport
    extends BaseTransport
    implements Acceptor, Connector
{
    @Override
    public void reply( MessageContext channel )
        throws IOException
    {
    }

    @Override
    public void sendOut( MessageContext context )
        throws IOException
    {
    }
}