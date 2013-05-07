package com.sanxing.sesame.dispatch;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.jbi.JBIException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.util.IntrospectionSupport;
import com.sanxing.sesame.util.URISupport;

public final class DispatcherProvider
{
    private static final Logger LOG = LoggerFactory.getLogger( DispatcherProvider.class );

    private static final FactoryFinder FINDER = new FactoryFinder( "META-INF/services/com/sanxing/sesame/dispatch/" );

    public static Dispatcher getDispatcher( String name )
        throws JBIException
    {
        String dispatcherName = getDispatcherName( name );
        try
        {
            Object value = FINDER.newInstance( dispatcherName );
            if ( value instanceof Dispatcher )
            {
                String query = getQuery( name );
                if ( query != null )
                {
                    Map map = URISupport.parseQuery( query );
                    if ( ( map != null ) && ( !( map.isEmpty() ) ) )
                    {
                        IntrospectionSupport.setProperties( value, map );
                    }
                }
                return ( (Dispatcher) value );
            }
            throw new JBIException( "No implementation found for: " + name );
        }
        catch ( IllegalAccessException e )
        {
            LOG.error( "getDispatcher(" + name + " failed: " + e, e );
            throw new JBIException( e );
        }
        catch ( InstantiationException e )
        {
            LOG.error( "getDispatcher(" + name + " failed: " + e, e );
            throw new JBIException( e );
        }
        catch ( IOException e )
        {
            LOG.error( "getDispatcher(" + name + " failed: " + e, e );
            throw new JBIException( e );
        }
        catch ( ClassNotFoundException e )
        {
            LOG.error( "getDispatcher(" + name + " failed: " + e, e );
            throw new JBIException( e );
        }
        catch ( URISyntaxException e )
        {
            LOG.error( "getDispatcher(" + name + " failed: " + e, e );
            throw new JBIException( e );
        }
    }

    public static String getDispatcherName( String str )
    {
        String result = str;
        int index = str.indexOf( 63 );
        if ( index >= 0 )
        {
            result = str.substring( 0, index );
        }
        return result;
    }

    protected static String getQuery( String str )
    {
        String result = null;
        int index = str.indexOf( 63 );
        if ( ( index >= 0 ) && ( index + 1 < str.length() ) )
        {
            result = str.substring( index + 1 );
        }
        return result;
    }
}