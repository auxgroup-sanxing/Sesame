package com.sanxing.sesame.core.jdbc;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class DataSourceInfo
    implements Serializable
{
    private static final long serialVersionUID = 4645521835991105237L;

    private String jndiName;

    private Element appInfo;

    private int transactionManager;

    public int getTransactionManager()
    {
        return transactionManager;
    }

    public void setTransactionManager( int transactionManager )
    {
        this.transactionManager = transactionManager;
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
        try
        {
            StringWriter sw = new StringWriter();
            XMLOutputter output = new XMLOutputter();
            output.setFormat( Format.getPrettyFormat() );
            output.output( getAppInfo(), sw );
            sw.flush();
            return "datasource : jndi-name:[" + getJndiName() + "] app-info [" + sw.toString() + "]";
        }
        catch ( IOException e )
        {
        }
        return "";
    }
}