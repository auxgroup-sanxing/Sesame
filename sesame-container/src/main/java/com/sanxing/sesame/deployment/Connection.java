package com.sanxing.sesame.deployment;

public class Connection
{
    private Consumer consumer;

    private Provider provider;

    public Consumer getConsumer()
    {
        return consumer;
    }

    public void setConsumer( Consumer consumer )
    {
        this.consumer = consumer;
    }

    public Provider getProvider()
    {
        return provider;
    }

    public void setProvider( Provider provider )
    {
        this.provider = provider;
    }
}