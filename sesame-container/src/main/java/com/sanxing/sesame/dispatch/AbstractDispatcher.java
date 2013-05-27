package com.sanxing.sesame.dispatch;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.JbiConstants;
import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.BaseLifeCycle;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.messaging.DeliveryChannelImpl;
import com.sanxing.sesame.messaging.ExchangePacket;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.router.Router;
import com.sanxing.sesame.servicedesc.InternalEndpoint;

public abstract class AbstractDispatcher
    extends BaseLifeCycle
    implements Dispatcher
{
    protected final Logger log;

    protected Router router;

    protected ExecutorFactory executorFactory;

    private final ReadWriteLock lock;

    private Thread suspendThread;

    private String name;

    public AbstractDispatcher()
    {
        log = LoggerFactory.getLogger( AbstractDispatcher.class );

        lock = new ReentrantReadWriteLock();
    }

    @Override
    public void init( Router router )
        throws JBIException
    {
        this.router = router;
        executorFactory = router.getContainer().getExecutorFactory();

        ObjectName objectName = router.getContainer().getManagementContext().createObjectName( this );
        try
        {
            router.getContainer().getManagementContext().registerMBean( objectName, this, LifeCycleMBean.class );
        }
        catch ( JMException e )
        {
            throw new JBIException( "Failed to register MBean with the ManagementContext", e );
        }
    }

    @Override
    public void start()
        throws JBIException
    {
        super.start();
    }

    @Override
    public void stop()
        throws JBIException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called dispatcher stop" );
        }
        if ( suspendThread != null )
        {
            suspendThread.interrupt();
        }
        super.stop();
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called dispatcher shutdown" );
        }
        router.getContainer().getManagementContext().unregisterMBean( this );
        super.shutDown();
    }

    @Override
    public void send( MessageExchange me )
        throws JBIException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called Dispatcher send" );
        }
        try
        {
            lock.readLock().lock();
            doSend( (MessageExchangeImpl) me );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public synchronized void suspend()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called dispatcher[" + getName() + "] suspend" );
        }
        lock.writeLock().lock();
        suspendThread = Thread.currentThread();
    }

    @Override
    public synchronized void resume()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called dispatcher[" + getName() + "] resume" );
        }
        lock.writeLock().unlock();
        suspendThread = null;
    }

    protected abstract void doSend( MessageExchangeImpl paramMessageExchangeImpl )
        throws JBIException;

    protected void doRouting( MessageExchangeImpl me )
        throws MessagingException
    {
        ComponentNameSpace id =
            ( me.getRole() == MessageExchange.Role.PROVIDER ) ? me.getDestinationId() : me.getSourceId();

        ComponentMBeanImpl lcc = router.getContainer().getRegistry().getComponent( id.getName() );

        if ( lcc != null )
        {
            if ( lcc.getDeliveryChannel() != null )
            {
                try
                {
                    lock.readLock().lock();
                    ( (DeliveryChannelImpl) lcc.getDeliveryChannel() ).processInBound( me );
                }
                finally
                {
                    lock.readLock().unlock();
                }
                return;
            }
            throw new MessagingException( "Component " + id.getName() + " is shut down" );
        }

        throw new MessagingException( "No component named " + id.getName() + " - Couldn't route MessageExchange " + me );
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "description", "The type of flow" );
        return AttributeInfoHelper.join( super.getAttributeInfos(), helper.getAttributeInfos() );
    }

    protected boolean isPersistent( MessageExchange me )
    {
        ExchangePacket packet = ( (MessageExchangeImpl) me ).getPacket();
        if ( packet.getPersistent() != null )
        {
            return packet.getPersistent().booleanValue();
        }
        return router.getContainer().isPersistent();
    }

    protected boolean isTransacted( MessageExchange me )
    {
        return ( me.getProperty( "javax.jbi.transaction.jta" ) != null );
    }

    protected boolean isSynchronous( MessageExchange me )
    {
        Boolean sync = (Boolean) me.getProperty( JbiConstants.SEND_SYNC );
        return ( ( sync != null ) && ( sync.booleanValue() ) );
    }

    protected boolean isClustered( MessageExchange me )
    {
        MessageExchangeImpl mei = (MessageExchangeImpl) me;
        if ( mei.getDestinationId() == null )
        {
            ServiceEndpoint se = me.getEndpoint();
            if ( se instanceof InternalEndpoint )
            {
                return ( (InternalEndpoint) se ).isClustered();
            }

            return false;
        }

        String destination = mei.getDestinationId().getContainerName();
        String source = mei.getSourceId().getContainerName();
        return ( !( source.equals( destination ) ) );
    }

    @Override
    public Router getRouter()
    {
        return router;
    }

    @Override
    public String getType()
    {
        return "Dispatcher";
    }

    @Override
    public String getName()
    {
        if ( name == null )
        {
            String n = super.getName();
            if ( n.endsWith( "Dispatcher" ) )
            {
                n = n.substring( 0, n.length() - 4 );
            }
            return n;
        }
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public ExecutorFactory getExecutorFactory()
    {
        return executorFactory;
    }
}