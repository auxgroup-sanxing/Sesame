package com.sanxing.sesame.binding.transport;

import java.io.IOException;
import java.net.URI;

import org.w3c.dom.Element;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.Carrier;
import com.sanxing.sesame.binding.context.MessageContext;

public class TransportProxy
    implements Acceptor, Connector
{
    private final Transport transport;

    private final URI uri;

    private long ref = 0L;

    protected TransportProxy( Transport transport, URI uri, Element config )
        throws IOException
    {
        this.transport = transport;
        this.transport.setURI( uri );
        this.transport.init( config );
        this.transport.open();

        this.uri = uri;
        ref = 1L;
    }

    protected Transport getReference()
    {
        ref += 1L;
        return this;
    }

    @Override
    public void close()
        throws IOException
    {
        ref -= 1L;

        if ( ref == 0L )
        {
            transport.close();
            String endpoint = uri.getScheme() + "://" + uri.getAuthority();
            TransportFactory.remove( endpoint );
        }
    }

    @Override
    public String getCharacterEncoding()
    {
        return transport.getCharacterEncoding();
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public boolean isActive()
    {
        return transport.isActive();
    }

    @Override
    public void init( Element config )
    {
        transport.init( config );
    }

    @Override
    public void open()
        throws IOException
    {
        if ( !( transport.isActive() ) )
        {
            transport.open();
        }
    }

    @Override
    public void removeCarrier( String contextPath, Carrier receiver )
    {
        transport.removeCarrier( contextPath, receiver );
    }

    @Override
    public void setConfig( String contextPath, Element config )
        throws IllegalArgumentException
    {
        transport.setConfig( contextPath, config );
    }

    @Override
    public void addCarrier( String contextPath, Carrier receiver )
    {
        transport.addCarrier( contextPath, receiver );
    }

    @Override
    public void setURI( URI uri )
    {
        throw new RuntimeException( "Illegal access, user can not set uri" );
    }

    @Override
    public boolean getKeepAlive()
    {
        return transport.getKeepAlive();
    }

    @Override
    public void setKeepAlive( boolean on )
    {
        transport.setKeepAlive( on );
    }

    @Override
    public void reply( MessageContext context )
        throws BindingException, IOException
    {
        if ( transport instanceof Connector )
        {
            ( (Connector) transport ).sendOut( context );
        }
    }

    @Override
    public void sendOut( MessageContext context )
        throws BindingException, IOException
    {
        if ( transport instanceof Connector )
        {
            ( (Connector) transport ).sendOut( context );
        }
    }

    @Override
    public int hashCode()
    {
        return transport.hashCode();
    }
}