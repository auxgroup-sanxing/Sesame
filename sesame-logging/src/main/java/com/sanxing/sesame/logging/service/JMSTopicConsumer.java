package com.sanxing.sesame.logging.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.logging.util.JNDIUtil;

public class JMSTopicConsumer
    implements MessageListener, ExceptionListener, Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( JMSTopicConsumer.class );

    private Session jmsSession;

    private Connection jmsConnection;

    private MessageConsumer topicConsumer = null;

    private final List<MessageConsumer> queueConsumers = new ArrayList();

    private int queueConsumerNum;

    private JMSQueueConsumer consumer = null;

    private MessageProducer queueProducer = null;

    private Session queueSession;

    private ObjectMessage queueMessage = null;

    static AtomicLong total = new AtomicLong( 0L );

    @Override
    public void run()
    {
        prepare();
        try
        {
            getConsumer().setMessageListener( this );
        }
        catch ( JMSException e )
        {
            e.printStackTrace();
        }
    }

    public MessageConsumer getConsumer()
    {
        return topicConsumer;
    }

    public void setConsumer( MessageConsumer consumer )
    {
        topicConsumer = consumer;
    }

    public JMSTopicConsumer()
    {
        consumer = new JMSQueueConsumer();
    }

    protected Object lookup( Context ctx, String name )
        throws NamingException
    {
        try
        {
            return ctx.lookup( name );
        }
        catch ( NameNotFoundException e )
        {
            LOG.error( "Could not find name [" + name + "]." );
            throw e;
        }
    }

    public void prepare()
    {
        try
        {
            Context jndi = JNDIUtil.getInitialContext();
            String cfname = "admin-QC";
            ConnectionFactory connFactory = (ConnectionFactory) lookup( jndi, cfname );
            jmsConnection = connFactory.createConnection();
            String clientID = "client-receiver";
            jmsConnection.setClientID( clientID );
            jmsSession = jmsConnection.createSession( false, 1 );
            String topicName = System.getProperty( "sesame.logging.monitor.jms.name", "LOGTOPIC" );
            Topic topic = jmsSession.createTopic( topicName );

            topicConsumer = jmsSession.createConsumer( topic );
            setConsumer( topicConsumer );

            String consumers = System.getProperty( "sesame.logging.monitor.consumers", "5" );
            queueConsumerNum = Integer.parseInt( consumers );

            Queue queue = jmsSession.createQueue( "INNERQUEUE" );
            for ( int i = 0; i < queueConsumerNum; ++i )
            {
                Session session = jmsConnection.createSession( false, 1 );
                MessageConsumer queueConsumer = session.createConsumer( queue );
                queueConsumers.add( queueConsumer );
            }
            for ( int i = 0; i < queueConsumerNum; ++i )
            {
                queueConsumers.get( i ).setMessageListener( consumer );
            }

            jmsConnection.start();
        }
        catch ( JMSException e )
        {
            e.printStackTrace();
        }
        catch ( NamingException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage( Message message )
    {
        try
        {
            LOG.debug( "Received: '" + message + "'" );

            queueSession = jmsConnection.createSession( false, 1 );
            Queue queue = queueSession.createQueue( "INNERQUEUE" );
            queueProducer = queueSession.createProducer( queue );
            queueProducer.setDeliveryMode( 2 );
            queueMessage = queueSession.createObjectMessage();

            if ( message instanceof ObjectMessage )
            {
                ObjectMessage objMsg = (ObjectMessage) message;
                Serializable obj = objMsg.getObject();
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Received object: " + obj );
                }
                queueMessage.clearBody();
                queueMessage.setObject( obj );
                queueProducer.send( queueMessage );
                if ( total.addAndGet( 1L ) % 1000L == 0L )
                {
                    LOG.info( "consumer " + total.get() );
                }
            }
            else
            {
                LOG.debug( "Received: '" + message + "'" );
            }

            queueSession.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onException( JMSException arg0 )
    {
    }

    public static void main( String[] argvs )
    {
        Executor executor = ExecutorFactory.getFactory().createExecutor( "services.registry" );
        JMSTopicConsumer receiver = new JMSTopicConsumer();
        executor.execute( receiver );
    }
}