package com.sanxing.sesame.dispatch;

import java.util.concurrent.Executor;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;

public class StraightThrough
    extends AbstractDispatcher
{
    public static final String name = "straight";

    private final Executor executor;

    public StraightThrough()
    {
        executor = ExecutorFactory.getFactory().createExecutor( "dispatcher" );
    }

    @Override
    protected void doSend( final MessageExchangeImpl me )
        throws MessagingException
    {
        if ( me.getDestinationId() == null )
        {
            me.setDestinationId( ( (AbstractEndpoint) me.getEndpoint() ).getComponentNameSpace() );
        }
        if ( me.getRole() == MessageExchange.Role.PROVIDER )
        {
            if ( ( me.getProperty( "sesame.exchange.thread.switch" ) != null )
                && ( ( (Boolean) me.getProperty( "sesame.exchange.thread.switch" ) ).booleanValue() ) )
            {
                executor.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            StraightThrough.this.doRouting( me );
                        }
                        catch ( MessagingException e )
                        {
                            me.setError( e );
                        }
                    }
                } );
            }
            else
            {
                doRouting( me );
            }
        }
        else
        {
            doRouting( me );
        }
    }

    @Override
    public String getName()
    {
        return "straight";
    }

    @Override
    public String getDescription()
    {
        return "Straight throutgh dispatcher";
    }

    @Override
    public boolean canHandle( MessageExchange me )
    {
        if ( isPersistent( me ) )
        {
            return false;
        }
        if ( isClustered( me ) )
        {
            return false;
        }

        return ( !( isTransacted( me ) ) );
    }
}