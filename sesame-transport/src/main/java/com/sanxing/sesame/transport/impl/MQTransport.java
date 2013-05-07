package com.sanxing.sesame.transport.impl;

import java.io.IOException;
import java.util.Map;

import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;

public class MQTransport
    extends BaseTransport
    implements Acceptor, Connector
{
    @Override
    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
    }

    @Override
    public void reply( MessageContext context )
        throws IOException
    {
    }

    @Override
    public void close()
        throws IOException
    {
    }

    @Override
    public String getCharacterEncoding()
    {
        return null;
    }

    @Override
    public boolean isActive()
    {
        return false;
    }

    @Override
    public void open()
        throws IOException
    {
    }

    @Override
    public void sendOut( MessageContext context )
        throws IOException
    {
    }
}