package com.sanxing.sesame.resolver;

import javax.jbi.messaging.MessageExchange;

public interface SubscriptionFilter
{
    public abstract boolean matches( MessageExchange exchange );
}