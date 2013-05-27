package com.sanxing.sesame.dispatch;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.messaging.MessageExchangeImpl;

public class DefaultChooser
    implements DispatcherChooser
{
    public static final String DEFAULT_DISPATCHER_NAME = DispatcherChooser.CLUSTER_DISPATCHER;

    private static Logger LOG = LoggerFactory.getLogger( DefaultChooser.class );

    @Override
    public Dispatcher chooseDispatcher( Dispatcher[] dispatchers, MessageExchange exchange )
        throws MessagingException
    {
        String dispatcher = (String) exchange.getProperty( ExchangeConst.DISPATCHER );
        LOG.debug( "dispatcher in message exchange :" + dispatcher );
        if ( dispatcher != null )
        {
            Dispatcher found = null;
            for ( int i = 0; i < dispatchers.length; ++i )
            {
                if ( dispatchers[i].getName().equalsIgnoreCase( dispatcher ) )
                {
                    found = dispatchers[i];
                    break;
                }
            }
            if ( found == null )
            {
                throw new MessagingException( "Dispatcher '" + dispatcher + "' was specified but not found" );
            }
            if ( found.canHandle( exchange ) )
            {
                return found;
            }

            throw new MessagingException( "Dispatcher '" + dispatcher
                + "' was specified but not able to handle exchange" );
        }

        if ( Platform.getEnv().isClustered() )
        {
            LOG.debug( "in cluster env.............." );
            for ( Dispatcher disp : dispatchers )
            {
                LOG.debug( "matching dispatcher " + disp.getName() );
                if ( disp.getName().equals( DispatcherChooser.CLUSTER_DISPATCHER ) )
                {
                    return disp;
                }
            }
        }
        else
        {
            LOG.debug( "in dev mode......." );
            for ( Dispatcher disp : dispatchers )
            {
                LOG.debug( "matching dispatcher " + disp.getName() );
                if ( disp.getName().equals( DispatcherChooser.STRAIGHT_DISPATCHER ) )
                {
                    return disp;
                }
            }

        }

        for ( int i = 0; i < dispatchers.length; ++i )
        {
            if ( dispatchers[i].canHandle( exchange ) )
            {
                ( (MessageExchangeImpl) exchange ).getPacket().setProperty( ExchangeConst.DISPATCHER,
                    dispatchers[i].getName() );
                return dispatchers[i];
            }
        }
        return null;
    }
}