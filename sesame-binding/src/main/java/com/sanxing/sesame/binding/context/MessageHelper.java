package com.sanxing.sesame.binding.context;

import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.Connector;

public class MessageHelper
{
    public static final String PROPETY_CARRIER = "carrier";

    public static final String PROPETY_BINDING = "binding";

    public static MessageContext createForAcceptor( Acceptor acceptor )
    {
        MessageContext context = new MessageContext();
        context.setTransport( acceptor );
        context.setAccepted( true );
        return context;
    }

    public static MessageContext createForConnector( Connector connector )
    {
        MessageContext context = new MessageContext();
        context.setTransport( connector );
        context.setAccepted( false );
        return context;
    }
}