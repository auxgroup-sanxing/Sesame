package com.sanxing.sesame.resolver;

import javax.jbi.messaging.MessageExchange;

public abstract interface SubscriptionFilter
{
    public abstract boolean matches( MessageExchange paramMessageExchange );
}