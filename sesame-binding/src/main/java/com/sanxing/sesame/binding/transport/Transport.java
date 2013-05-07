package com.sanxing.sesame.binding.transport;

import java.io.IOException;
import java.net.URI;

import org.w3c.dom.Element;

import com.sanxing.sesame.binding.Carrier;

public abstract interface Transport
{
    public abstract void init( Element paramElement );

    public abstract void open()
        throws IOException;

    public abstract void close()
        throws IOException;

    public abstract boolean isActive();

    public abstract URI getURI();

    public abstract void setURI( URI paramURI );

    public abstract void addCarrier( String paramString, Carrier paramCarrier );

    public abstract void removeCarrier( String paramString, Carrier paramCarrier );

    public abstract String getCharacterEncoding();

    public abstract void setConfig( String paramString, Element paramElement )
        throws IllegalArgumentException;

    public abstract boolean getKeepAlive();

    public abstract void setKeepAlive( boolean paramBoolean );
}