package com.sanxing.sesame.dispatch;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public abstract interface DispatcherChooser
{
    public static final String CLUSTER_DISPATCHER = "cluster";

    public static final String STRAIGHT_DISPATCHER = "straight";

    public abstract Dispatcher chooseDispatcher( Dispatcher[] paramArrayOfDispatcher,
                                                 MessageExchange paramMessageExchange )
        throws MessagingException;
}