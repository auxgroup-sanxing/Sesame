package com.sanxing.sesame.binding.transport;

import java.io.IOException;
import java.net.URI;

import org.w3c.dom.Element;

import com.sanxing.sesame.binding.Carrier;

public interface Transport
{
    public abstract void init( Element element );

    public abstract void open()
        throws IOException;

    public abstract void close()
        throws IOException;

    public abstract boolean isActive();

    public abstract URI getURI();

    public abstract void setURI( URI uri );

    public abstract void addCarrier( String contextPath, Carrier carrier );

    public abstract void removeCarrier( String contextPath, Carrier carrier );

    public abstract String getCharacterEncoding();

    public abstract void setConfig( String contextPath, Element config )
        throws IllegalArgumentException;

    public abstract boolean getKeepAlive();

    public abstract void setKeepAlive( boolean on );
}