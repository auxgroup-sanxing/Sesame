package com.sanxing.sesame.core.jms;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

public class JMSServiceInfo
    implements Serializable
{
    private static final long serialVersionUID = 473082292433341293L;

    public String serverName;

    private Element appInfo;

    private List<JMSResourceEntry> entries = new LinkedList();

    public Element getAppInfo()
    {
        return appInfo;
    }

    public void setAppInfo( Element appInfo )
    {
        this.appInfo = appInfo;
    }

    public String getServerName()
    {
        return serverName;
    }

    public void setServerName( String serverName )
    {
        this.serverName = serverName;
    }

    public List<JMSResourceEntry> getEntries()
    {
        return entries;
    }

    public void setEntries( List<JMSResourceEntry> entries )
    {
        this.entries = entries;
    }

    @Override
    public String toString()
    {
        String temp = "JMSServiceInfo [appInfo=" + appInfo + ", entries=";
        for ( JMSResourceEntry entry : entries )
        {
            temp = temp + entry;
        }
        return temp;
    }
}