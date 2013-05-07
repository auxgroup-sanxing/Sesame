package com.sanxing.sesame.messaging;

import javax.jbi.messaging.Fault;

public class FaultImpl
    extends NormalizedMessageImpl
    implements Fault
{
    private static final long serialVersionUID = -2369080326592427325L;

    public FaultImpl()
    {
    }

    public FaultImpl( MessageExchangeImpl exchange )
    {
        super( exchange );
    }
}