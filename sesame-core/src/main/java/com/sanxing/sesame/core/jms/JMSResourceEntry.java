package com.sanxing.sesame.core.jms;

import java.io.Serializable;

import org.jdom.Element;

public class JMSResourceEntry
    implements Serializable
{
    private static final long serialVersionUID = 2031634080016834510L;

    public static final String TOPIC = "topic";

    public static final String QUEUE = "queue";

    public static final String CONNECTION_FACTORY = "connection-factory";

    public static final String QUEUE_CONNECTION_FACTORY = "queue-connection-factory";

    public static final String TOPIC_CONNECTION_FACTORY = "topic-connection-factory";

    private String type;

    private String jndiName;

    private Element appInfo;

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getJndiName()
    {
        return jndiName;
    }

    public void setJndiName( String jndiName )
    {
        this.jndiName = jndiName;
    }

    public Element getAppInfo()
    {
        return appInfo;
    }

    public void setAppInfo( Element appInfo )
    {
        this.appInfo = appInfo;
    }

    @Override
    public String toString()
    {
        return "JMSResourceEntry [jndiName=" + jndiName + ", type=" + type + "]";
    }
}