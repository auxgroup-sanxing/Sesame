package com.sanxing.sesame.transport.impl;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.jbi.messaging.MessagingException;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.executors.ExecutorFactory;

public class JMSTransport
    extends BaseTransport
    implements Acceptor, Connector, MessageListener, ExceptionListener
{
    private static Logger LOG = LoggerFactory.getLogger( JMSTransport.class );

    private boolean active = false;

    private Executor executor;

    private ConnectionFactory connectionFactory;

    private Connection connection;

    private Session sendSession;

    private Session recvSession;

    private Destination requestQ;

    private Destination responseQ;

    private MessageProducer producer;

    private MessageConsumer receiver;

    private String initialContextFactoryName;

    private String providerURL;

    private String connectionFactoryBindingName;

    private String requestQBindingName;

    private String responseQBindingName;

    private void init()
        throws Exception
    {
        Hashtable env = new Hashtable();
        env.put( "java.naming.factory.initial", initialContextFactoryName );
        env.put( "java.naming.provider.url", providerURL );
        InitialDirContext ctx = new InitialDirContext( env );

        connectionFactory = ( (ConnectionFactory) ctx.lookup( connectionFactoryBindingName ) );
        requestQ = ( (Destination) ctx.lookup( requestQBindingName ) );
        responseQ = ( (Destination) ctx.lookup( responseQBindingName ) );
    }

    @Override
    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
        initialContextFactoryName = getProperty( "initialContextFactoryName", properties );
        providerURL = getProperty( "providerURL", properties );
        connectionFactoryBindingName = getProperty( "connectionFactoryBindingName", properties );
        requestQBindingName = getProperty( "requestQBindingName", properties );
        responseQBindingName = getProperty( "responseQBindingName", properties );
    }

    public String getProperty( String key, Map<?, ?> properties )
    {
        return ( (String) properties.get( key ) );
    }

    @Override
    public void reply( MessageContext context )
        throws IOException
    {
        try
        {
            if ( context == null )
            {
                TextMessage message = sendSession.createTextMessage();
                message.setText( "receive acknowledge" );
                producer.send( message );
                return;
            }
            BinaryResult result = (BinaryResult) context.getResult();
            byte[] bytes = result.getBytes();
            LOG.debug( "response is:" + new String( bytes ) );
            StreamMessage respMsg = sendSession.createStreamMessage();
            respMsg.writeBytes( bytes );
            producer.send( respMsg );
        }
        catch ( JMSException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void close()
        throws IOException
    {
        active = false;
        try
        {
            connection.close();
        }
        catch ( JMSException e )
        {
            throw new IOException( e.getMessage() );
        }
        finally
        {
            connection = null;
            executor = null;
        }
    }

    @Override
    public String getCharacterEncoding()
    {
        return "utf-8";
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public void open()
        throws IOException
    {
        try
        {
            executor = ExecutorFactory.getFactory().createExecutor( "transports.jms" );

            init();

            connection = connectionFactory.createConnection();
            connection.setExceptionListener( this );

            sendSession = connection.createSession( false, 1 );
            recvSession = connection.createSession( false, 1 );

            producer = sendSession.createProducer( responseQ );
            receiver = recvSession.createConsumer( requestQ );

            receiver.setMessageListener( this );

            connection.start();

            active = true;
        }
        catch ( JMSException e )
        {
            Exception linkedE = e.getLinkedException();
            if ( linkedE != null )
            {
                linkedE.printStackTrace();
            }
            throw new IOException( e.getMessage() );
        }
        catch ( Exception e )
        {
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void onMessage( Message msg )
    {
        try
        {
            if ( msg instanceof TextMessage )
            {
                LOG.debug( "get text msg is:" + ( (TextMessage) msg ).getText() );

                return;
            }

            if ( msg instanceof BytesMessage )
            {
                BytesMessage obj = (BytesMessage) msg;
                byte[] getMsg = new byte[1024];
                int len = obj.readBytes( getMsg );
                executor.execute( new Command( getMsg, len ) );
                return;
            }
            if ( msg instanceof StreamMessage )
            {
                StreamMessage obj = (StreamMessage) msg;

                byte[] getMsg = new byte[1024];
                int len = obj.readBytes( getMsg );
                executor.execute( new Command( getMsg, len ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    @Override
    public void sendOut( MessageContext context )
        throws IOException
    {
        LOG.debug( "sendout.............................." );
    }

    @Override
    public void onException( JMSException e )
    {
        LOG.debug( e.getMessage() );
        Exception linkedE = e.getLinkedException();
        if ( linkedE != null )
        {
            LOG.debug( linkedE.getMessage() );
        }
        sendSession = null;
        recvSession = null;
        producer = null;
        receiver = null;
        try
        {
            do
            {
                try
                {
                    Thread.sleep( 300000L );
                }
                catch ( InterruptedException e2 )
                {
                    e2.printStackTrace();
                }
                sendSession = connection.createSession( false, 1 );
                recvSession = connection.createSession( false, 1 );

                producer = sendSession.createProducer( responseQ );
                receiver = recvSession.createConsumer( requestQ );

                receiver.setMessageListener( this );
                if ( sendSession != null )
                {
                    return;
                }
            }
            while ( active );
        }
        catch ( JMSException e1 )
        {
            LOG.debug( e.getMessage() );
            Exception el = e.getLinkedException();
            if ( el != null )
            {
                LOG.debug( el.getStackTrace().toString() );
            }
        }
    }

    private class Command
        implements Runnable
    {
        byte[] getMsg;

        int len;

        Command( byte[] paramArrayOfByte, int paramInt )
        {
            getMsg = paramArrayOfByte;
            len = paramInt;
        }

        @Override
        public void run()
        {
            BinarySource input = new BinarySource();
            input.setEncoding( getCharacterEncoding() );
            MessageContext ctx = new MessageContext( JMSTransport.this, input );
            try
            {
                ctx.setPath( "/" );
                ctx.setAction( "trancode" );
                input.setBytes( getMsg, len );

                postMessage( ctx );
                reply( ctx );
            }
            catch ( MessagingException e )
            {
                JMSTransport.LOG.error( e.getMessage(), e );
                ctx.setException( e );
                try
                {
                    reply( ctx );
                }
                catch ( IOException ex )
                {
                    JMSTransport.LOG.error( ex.getMessage(), ex );
                }
            }
            catch ( BindingException e )
            {
                JMSTransport.LOG.error( e.getMessage(), e );
                ctx.setException( e );
                try
                {
                    reply( ctx );
                }
                catch ( IOException ex )
                {
                    JMSTransport.LOG.error( ex.getMessage(), ex );
                }
            }
            catch ( IOException e )
            {
                JMSTransport.LOG.error( e.getMessage(), e );
            }
        }
    }
}