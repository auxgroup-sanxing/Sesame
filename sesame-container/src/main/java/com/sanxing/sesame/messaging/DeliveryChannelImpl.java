package com.sanxing.sesame.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.exception.ExchangeTimeoutException;
import com.sanxing.sesame.listener.MessageExchangeListener;
import com.sanxing.sesame.mbean.ComponentContextImpl;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.uuid.IdGenerator;

public class DeliveryChannelImpl
    implements DeliveryChannel
{
    private static final Logger LOG = LoggerFactory.getLogger( DeliveryChannelImpl.class );

    private JBIContainer container;

    private ComponentContextImpl context;

    private final ComponentMBeanImpl component;

    private final BlockingQueue<MessageExchangeImpl> queue;

    private final IdGenerator idGenerator = new IdGenerator();

    private MessageExchangeFactory inboundFactory;

    private int intervalCount;

    private final AtomicBoolean closed = new AtomicBoolean( false );

    private final Map<Thread, Boolean> waiters = new ConcurrentHashMap();

    private final TransactionManager transactionManager;

    private final Map<String, MessageExchangeImpl> exchangesById = new ConcurrentHashMap();

    public DeliveryChannelImpl( ComponentMBeanImpl component )
    {
        this.component = component;
        container = component.getContainer();
        queue = new ArrayBlockingQueue( component.getInboundQueueCapacity() );
        transactionManager = ( (TransactionManager) container.getTransactionManager() );
    }

    public int getQueueSize()
    {
        return queue.size();
    }

    @Override
    public void close()
        throws MessagingException
    {
        if ( closed.compareAndSet( false, true ) )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Closing DeliveryChannel " + this );
            }
            List<MessageExchangeImpl> pending = new ArrayList( queue.size() );
            queue.drainTo( pending );
            for ( MessageExchangeImpl messageExchange : pending )
            {
                if ( ( messageExchange.getTransactionContext() != null )
                    && ( messageExchange.getMirror().getSyncState() == 1 ) )
                {
                    notifyExchange( messageExchange.getMirror(), messageExchange.getMirror(), "close" );
                }
            }

            Thread[] threads = waiters.keySet().toArray( new Thread[waiters.size()] );
            for ( int i = 0; i < threads.length; ++i )
            {
                threads[i].interrupt();
            }

            ServiceEndpoint[] endpoints =
                container.getRegistry().getEndpointsForComponent( component.getComponentNameSpace() );
            for ( int i = 0; i < endpoints.length; ++i )
            {
                try
                {
                    component.getContext().deactivateEndpoint( endpoints[i] );
                }
                catch ( JBIException e )
                {
                    LOG.error( "Error deactivating endpoint", e );
                }
            }
        }
    }

    protected void checkNotClosed()
        throws MessagingException
    {
        if ( closed.get() )
        {
            throw new MessagingException( this + " has been closed." );
        }
    }

    @Override
    public MessageExchangeFactory createExchangeFactory()
    {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setContext( context );
        ActivationSpec activationSpec = context.getActivationSpec();
        if ( activationSpec != null )
        {
            String componentName = context.getComponentNameSpace().getName();

            QName serviceName = activationSpec.getDestinationService();
            if ( serviceName != null )
            {
                result.setServiceName( serviceName );
                LOG.debug( "default destination serviceName for " + componentName + " = " + serviceName );
            }
            QName interfaceName = activationSpec.getDestinationInterface();
            if ( interfaceName != null )
            {
                result.setInterfaceName( interfaceName );
                LOG.debug( "default destination interfaceName for " + componentName + " = " + interfaceName );
            }
            QName operationName = activationSpec.getDestinationOperation();
            if ( operationName != null )
            {
                result.setOperationName( operationName );
                LOG.debug( "default destination operationName for " + componentName + " = " + operationName );
            }
            String endpointName = activationSpec.getDestinationEndpoint();
            if ( endpointName != null )
            {
                boolean endpointSet = false;
                LOG.debug( "default destination endpointName for " + componentName + " = " + endpointName );
                if ( ( serviceName != null ) && ( endpointName != null ) )
                {
                    endpointName = endpointName.trim();
                    ServiceEndpoint endpoint = container.getRegistry().getEndpoint( serviceName, endpointName );
                    if ( endpoint != null )
                    {
                        result.setEndpoint( endpoint );
                        LOG.info( "Set default destination endpoint for " + componentName + " to " + endpoint );
                        endpointSet = true;
                    }
                }
                if ( !( endpointSet ) )
                {
                    LOG.warn( "Could not find destination endpoint for " + componentName + " service(" + serviceName
                        + ") with endpointName " + endpointName );
                }
            }
        }

        return result;
    }

    @Override
    public MessageExchangeFactory createExchangeFactory( QName interfaceName )
    {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setInterfaceName( interfaceName );
        return result;
    }

    @Override
    public MessageExchangeFactory createExchangeFactoryForService( QName serviceName )
    {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setServiceName( serviceName );
        return result;
    }

    @Override
    public MessageExchangeFactory createExchangeFactory( ServiceEndpoint endpoint )
    {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setEndpoint( endpoint );
        return result;
    }

    protected MessageExchangeFactoryImpl createMessageExchangeFactory()
    {
        MessageExchangeFactoryImpl messageExchangeFactory = new MessageExchangeFactoryImpl( idGenerator, closed );
        messageExchangeFactory.setContext( context );
        return messageExchangeFactory;
    }

    @Override
    public MessageExchange accept()
        throws MessagingException
    {
        return accept( 9223372036854775807L );
    }

    @Override
    public MessageExchange accept( long timeoutMS )
        throws MessagingException
    {
        try
        {
            checkNotClosed();
            MessageExchangeImpl me = queue.poll( timeoutMS, TimeUnit.MILLISECONDS );
            if ( me != null )
            {
                if ( me.getPacket().isAborted() )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( "Aborted " + me.getExchangeId() + " in " + this );
                    }
                    me = null;
                }
                else
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( "Accepting " + me.getExchangeId() + " in " + this );
                    }

                    if ( ( me.getTxLock() != null ) && ( me.getStatus() != ExchangeStatus.ACTIVE ) )
                    {
                        notifyExchange( me.getMirror(), me.getTxLock(), "acceptFinishedExchangeWithTxLock" );
                        me.handleAccept();
                        if ( LOG.isTraceEnabled() )
                        {
                            LOG.trace( "Accepted: " + me );
                        }
                    }
                    else if ( ( me.isTransacted() ) && ( me.getStatus() != ExchangeStatus.ACTIVE ) )
                    {
                        me.handleAccept();
                        if ( LOG.isTraceEnabled() )
                        {
                            LOG.trace( "Accepted: " + me );
                        }
                    }
                    else
                    {
                        resumeTx( me );
                        me.handleAccept();
                        if ( LOG.isTraceEnabled() )
                        {
                            LOG.trace( "Accepted: " + me );
                        }
                    }
                }
            }

            return me;
        }
        catch ( InterruptedException e )
        {
            throw new MessagingException( "accept failed", e );
        }
    }

    protected void autoSetPersistent( MessageExchangeImpl me )
    {
        Boolean persistent = me.getPersistent();
        if ( persistent == null )
        {
            if ( context.getActivationSpec().getPersistent() != null )
            {
                persistent = context.getActivationSpec().getPersistent();
            }
            else
            {
                persistent = Boolean.valueOf( context.getContainer().isPersistent() );
            }
            me.setPersistent( persistent );
        }
    }

    protected void throttle()
    {
        if ( component.isExchangeThrottling() )
        {
            if ( component.getThrottlingInterval() > intervalCount )
            {
                intervalCount = 0;
                try
                {
                    Thread.sleep( component.getThrottlingTimeout() );
                }
                catch ( InterruptedException e )
                {
                    LOG.warn( "throttling failed", e );
                }
            }
            intervalCount += 1;
        }
    }

    protected void doSend( MessageExchangeImpl me, boolean sync )
        throws MessagingException
    {
        MessageExchangeImpl mirror = me.getMirror();
        boolean finished = me.getStatus() != ExchangeStatus.ACTIVE;
        try
        {
            if ( me.getPacket().isAborted() )
            {
                throw new ExchangeTimeoutException( me );
            }

            autoEnlistInTx( me );

            autoSetPersistent( me );

            throttle();

            if ( me.getRole() == MessageExchange.Role.CONSUMER )
            {
                me.setSourceId( component.getComponentNameSpace() );
            }

            me.handleSend( sync );
            mirror.setTxState( 0 );

            if ( ( finished ) && ( me.getTxLock() == null ) && ( me.getTxState() == 2 ) && ( !( me.isPushDelivery() ) )
                && ( me.getRole() == MessageExchange.Role.CONSUMER ) )
            {
                me.setTransactionContext( null );
            }

            container.getRouter().sendExchangePacket( mirror );
        }
        catch ( MessagingException e )
        {
        }
        catch ( JBIException e )
        {
        }
        finally
        {
            if ( me.getTxLock() != null )
            {
                if ( mirror.getTxState() == 1 )
                {
                    suspendTx( mirror );
                }
                synchronized ( me.getTxLock() )
                {
                    notifyExchange( me, me.getTxLock(), "doSendWithTxLock" );
                }
            }
        }
    }

    @Override
    public void send( MessageExchange messageExchange )
        throws MessagingException
    {
        checkNotClosed();

        messageExchange.setProperty( "javax.jbi.messaging.sendSync", null );
        MessageExchangeImpl me = (MessageExchangeImpl) messageExchange;
        doSend( me, false );
    }

    @Override
    public boolean sendSync( MessageExchange messageExchange )
        throws MessagingException
    {
        return sendSync( messageExchange, 0L );
    }

    @Override
    public boolean sendSync( MessageExchange messageExchange, long timeout )
        throws MessagingException
    {
        checkNotClosed();

        boolean result = false;

        messageExchange.setProperty( "javax.jbi.messaging.sendSync", Boolean.TRUE );

        MessageExchangeImpl me = (MessageExchangeImpl) messageExchange;
        String exchangeKey = me.getKey();
        try
        {
            exchangesById.put( exchangeKey, me );

            synchronized ( me )
            {
                doSend( me, true );
                if ( me.getSyncState() != 2 )
                {
                    waitForExchange( me, me, timeout, "sendSync" );
                }
                else if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Exchange " + messageExchange.getExchangeId()
                        + " has already been answered (no need to wait)" );
                }
            }

            if ( me.getSyncState() == 2 )
            {
                me.handleAccept();

                resumeTx( me );

                result = true;
            }
            else
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Exchange " + messageExchange.getExchangeId() + " has been aborted" );
                }
                me.getPacket().setAborted( true );
                result = false;
            }
        }
        catch ( InterruptedException e )
        {
        }
        catch ( RuntimeException e )
        {
        }
        finally
        {
            exchangesById.remove( exchangeKey );
        }
        return result;
    }

    public JBIContainer getContainer()
    {
        return container;
    }

    public void setContainer( JBIContainer container )
    {
        this.container = container;
    }

    public ComponentMBeanImpl getComponent()
    {
        return component;
    }

    public ComponentContextImpl getContext()
    {
        return context;
    }

    public void setContext( ComponentContextImpl context )
    {
        this.context = context;
    }

    public void processInBound( MessageExchangeImpl me )
        throws MessagingException
    {
        if ( LOG.isTraceEnabled() )
        {
            LOG.trace( "Processing inbound exchange: " + me );
        }

        checkNotClosed();

        MessageExchangeImpl original = exchangesById.get( me.getKey() );
        if ( ( original != null ) && ( me != original ) )
        {
            original.copyFrom( me );
            me = original;
        }

        if ( me.getSyncState() == 1 )
        {
            suspendTx( original );
            me.setSyncState( 2 );
            notifyExchange( original, original, "processInboundSynchronousExchange" );
            return;
        }

        MessageExchangeListener listener = getExchangeListener();
        if ( listener != null )
        {
            me.handleAccept();

            me.setPushDeliver( true );

            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try
            {
                Thread.currentThread().setContextClassLoader( component.getComponent().getClass().getClassLoader() );
                listener.onMessageExchange( me );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( old );
            }

            return;
        }

        if ( ( me.isTransacted() ) && ( me.getStatus() == ExchangeStatus.ACTIVE ) )
        {
            if ( me.getTxState() == 2 )
            {
                try
                {
                    suspendTx( me );
                    queue.put( me );
                }
                catch ( InterruptedException e )
                {
                    LOG.debug( "Exchange " + me.getExchangeId() + " aborted due to thread interruption", e );
                    me.getPacket().setAborted( true );
                }

            }
            else
            {
                Object lock = new Object();
                synchronized ( lock )
                {
                    try
                    {
                        me.setTxLock( lock );
                        suspendTx( me );
                        queue.put( me );
                        waitForExchange( me, lock, 0L, "processInboundTransactionalExchange" );
                    }
                    catch ( InterruptedException e )
                    {
                        LOG.debug( "Exchange " + me.getExchangeId() + " aborted due to thread interruption", e );
                        me.getPacket().setAborted( true );
                    }
                    finally
                    {
                        me.setTxLock( null );
                        resumeTx( me );
                    }
                }

            }

        }
        else
        {
            try
            {
                queue.put( me );
            }
            catch ( InterruptedException e )
            {
                LOG.debug( "Exchange " + me.getExchangeId() + " aborted due to thread interruption", e );
                me.getPacket().setAborted( true );
            }
        }
    }

    protected MessageExchangeListener getExchangeListener()
    {
        Component comp = component.getComponent();
        if ( comp instanceof MessageExchangeListener )
        {
            return ( (MessageExchangeListener) comp );
        }
        ComponentLifeCycle lifecycle = component.getLifeCycle();
        if ( lifecycle instanceof MessageExchangeListener )
        {
            return ( (MessageExchangeListener) lifecycle );
        }
        return null;
    }

    protected void waitForExchange( MessageExchangeImpl me, Object lock, long timeout, String from )
        throws InterruptedException
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Waiting for exchange " + me.getExchangeId() + " (" + Integer.toHexString( me.hashCode() )
                + ") to be answered in " + this + " from " + from );
        }

        Thread th = Thread.currentThread();
        try
        {
            waiters.put( th, Boolean.TRUE );
            lock.wait( timeout );
        }
        finally
        {
            waiters.remove( th );
        }
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Notified: " + me.getExchangeId() + "(" + Integer.toHexString( me.hashCode() ) + ") in " + this
                + " from " + from );
        }
    }

    protected void notifyExchange( MessageExchangeImpl me, Object lock, String from )
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Notifying exchange " + me.getExchangeId() + "(" + Integer.toHexString( me.hashCode() )
                + ") in " + this + " from " + from );
        }

        synchronized ( lock )
        {
            lock.notify();
        }
    }

    public MessageExchangeFactory getInboundFactory()
    {
        if ( inboundFactory == null )
        {
            inboundFactory = createExchangeFactory();
        }
        return inboundFactory;
    }

    protected void suspendTx( MessageExchangeImpl me )
    {
        if ( transactionManager == null )
        {
            return;
        }
        try
        {
            Transaction oldTx = me.getTransactionContext();
            if ( oldTx != null )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Suspending transaction for " + me.getExchangeId() + " in " + this );
                }
                Transaction tx = transactionManager.suspend();
                if ( tx != oldTx )
                {
                    throw new IllegalStateException(
                        "the transaction context set in the messageExchange is not bound to the current thread" );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.info( "Exchange " + me.getExchangeId() + " aborted due to transaction exception", e );
            me.getPacket().setAborted( true );
        }
    }

    protected void resumeTx( MessageExchangeImpl me )
        throws MessagingException
    {
        if ( transactionManager == null )
        {
            return;
        }
        try
        {
            Transaction oldTx = me.getTransactionContext();
            if ( oldTx != null )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Resuming transaction for " + me.getExchangeId() + " in " + this );
                }
                transactionManager.resume( oldTx );
            }
        }
        catch ( Exception e )
        {
            throw new MessagingException( e );
        }
    }

    protected void autoEnlistInTx( MessageExchangeImpl me )
        throws MessagingException
    {
        if ( ( transactionManager == null ) || ( !( container.isAutoEnlistInTransaction() ) ) )
        {
            return;
        }
        try
        {
            Transaction tx = transactionManager.getTransaction();
            if ( tx != null )
            {
                Object oldTx = me.getTransactionContext();
                if ( oldTx == null )
                {
                    me.setTransactionContext( tx );
                }
                else if ( oldTx != tx )
                {
                    throw new IllegalStateException(
                        "the transaction context set in the messageExchange is not bound to the current thread" );
                }
            }
        }
        catch ( Exception e )
        {
            throw new MessagingException( e );
        }
    }

    @Override
    public String toString()
    {
        return "DeliveryChannel{" + component.getName() + "}";
    }

    public void cancelPendingExchanges()
    {
        for ( String id : exchangesById.keySet() )
        {
            MessageExchange exchange = exchangesById.get( id );
            synchronized ( exchange )
            {
                exchange.notifyAll();
            }
        }
    }
}