package com.sanxing.sesame.binding.transport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;

import com.sanxing.sesame.address.Location;
import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.annotation.Description;

public class TransportFactory
{
    private static Map<String, Class<? extends Acceptor>> acceptors = new Hashtable();

    private static Map<String, Class<? extends Connector>> connectors = new Hashtable();

    private static Map<String, TransportProxy> transports = new ConcurrentHashMap();

    public static Transport getTransport( Location location )
        throws BindingException
    {
        return getTransport( location.getURI(), location.getConfig(), location.getStyle() );
    }

    public static Transport getTransport( URI uri, Element config, String style )
        throws BindingException
    {
        String endpoint = uri.getScheme() + "://" + uri.getAuthority();
        TransportProxy proxy = transports.get( endpoint );
        if ( proxy != null )
        {
            return proxy.getReference();
        }

        String scheme = uri.getScheme();

        Class clazz = null;
        if ( scheme == null )
        {
            throw new BindingException( "Invalid URL '" + uri + "', scheme not specified" );
        }
        if ( style == null )
        {
            throw new NullPointerException( "Transport style can not be null" );
        }
        if ( "local".equals( style ) )
        {
            clazz = acceptors.get( scheme );
        }
        else if ( "remote".equals( style ) )
        {
            clazz = connectors.get( scheme );
        }
        else
        {
            throw new BindingException( "Unknown transport style '" + style + "'" );
        }

        if ( clazz == null )
        {
            throw new BindingException( "Can not find transport for scheme '" + scheme + "'" );
        }
        try
        {
            Constructor constructor = clazz.getConstructor( new Class[0] );
            Transport transport = (Transport) constructor.newInstance( new Object[0] );
            proxy = new TransportProxy( transport, uri, config );
            transports.put( endpoint, proxy );
            return proxy;
        }
        catch ( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            throw new BindingException( t.getMessage(), t );
        }
        catch ( Exception e )
        {
            throw new BindingException( e.getMessage(), e );
        }
    }

    public static String[] getSchemes()
    {
        Set schemes = new HashSet();
        schemes.addAll( acceptors.keySet() );
        schemes.addAll( connectors.keySet() );
        return ( (String[]) schemes.toArray( new String[schemes.size()] ) );
    }

    public static String getSchemeDescription( String scheme )
    {
        Class acceptorClazz = acceptors.get( scheme );
        if ( acceptorClazz != null )
        {
            Description description = (Description) acceptorClazz.getAnnotation( Description.class );
            return description.value();
        }
        Class connectorClazz = connectors.get( scheme );
        if ( connectorClazz != null )
        {
            Description description = (Description) connectorClazz.getAnnotation( Description.class );
            return description.value();
        }
        return null;
    }

    public static void register( String scheme, Class<? extends Transport> clazz )
    {
        if ( Acceptor.class.isAssignableFrom( clazz ) )
        {
            if ( acceptors.containsKey( scheme ) )
            {
                throw new RuntimeException( "Acceptor for Scheme [" + scheme + "] already registered!" );
            }
            acceptors.put( scheme, clazz.asSubclass( Acceptor.class ) );
        }

        if ( Connector.class.isAssignableFrom( clazz ) )
        {
            if ( connectors.containsKey( scheme ) )
            {
                throw new RuntimeException( "Connector for Scheme [" + scheme + "] already registered!" );
            }
            connectors.put( scheme, clazz.asSubclass( Connector.class ) );
        }
    }

    public static void unregister( String scheme, Class<? extends Transport> clazz )
    {
        if ( Acceptor.class.isAssignableFrom( clazz ) )
        {
            acceptors.remove( scheme );
        }
        if ( Connector.class.isAssignableFrom( clazz ) )
        {
            connectors.remove( scheme );
        }
    }

    protected static void remove( String authority )
    {
        transports.remove( authority );
    }
}