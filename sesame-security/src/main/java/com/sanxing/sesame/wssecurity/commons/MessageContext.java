package com.sanxing.sesame.wssecurity.commons;

import java.util.Iterator;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;

public class MessageContext
    implements SOAPMessageContext
{
    private SOAPMessage soapMessage;

    private String[] Roles;

    public SOAPMessage getMessage()
    {
        return this.soapMessage;
    }

    public void setMessage( SOAPMessage soapMessage )
    {
        this.soapMessage = soapMessage;
    }

    public void setRoles( String[] para )
    {
        this.Roles = para;
    }

    public String[] getRoles()
    {
        return this.Roles;
    }

    public boolean containsProperty( String name )
    {
        return false;
    }

    public Object getProperty( String name )
    {
        return null;
    }

    public Iterator getPropertyNames()
    {
        return null;
    }

    public void removeProperty( String name )
    {
    }

    public void setProperty( String name, Object value )
    {
    }
}