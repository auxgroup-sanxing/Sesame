/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.logging;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.naming.JNDIUtil;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * @author ShangjieZhou
 */
public class SesameJMSAppender
    extends AsyncAppender
{
    private static final Logger LOG = LoggerFactory.getLogger( SesameJMSAppender.class );

    String securityPrincipalName;

    String securityCredentials;

    String initialContextFactoryName;

    String urlPkgPrefixes;

    String providerURL;

    String topicBindingName;

    String tcfBindingName;

    String userName;

    String password;

    boolean locationInfo;

    Connection connection;

    Session session;

    MessageProducer producer;

    private String cfname;

    private String qname;

    public SesameJMSAppender()
    {
    }

    public SesameJMSAppender( String cfname, String qname )
    {
        this.cfname = cfname;
        this.qname = qname;
        activateOptions();
    }

    public void setTopicConnectionFactoryBindingName( String tcfBindingName )
    {
        this.tcfBindingName = tcfBindingName;
    }

    public String getTopicConnectionFactoryBindingName()
    {
        return this.tcfBindingName;
    }

    public void setTopicBindingName( String topicBindingName )
    {
        this.topicBindingName = topicBindingName;
    }

    public String getTopicBindingName()
    {
        return this.topicBindingName;
    }

    public boolean getLocationInfo()
    {
        return this.locationInfo;
    }

    public void activateOptions()
    {
        try
        {
            Context jndi = JNDIUtil.getInitialContext();
            ConnectionFactory connFactory = (ConnectionFactory) lookup( jndi, this.cfname );
            this.connection = connFactory.createConnection();
            String clientID = "client-sender";
            this.connection.setClientID( clientID );
            this.session = this.connection.createSession( false, 1 );

            String topicName = System.getProperty( "sesame.logging.monitor.jms.name", "LOGTOPIC" );
            Destination topic = this.session.createTopic( topicName );
            this.producer = this.session.createProducer( topic );

            this.producer.setDeliveryMode( 1 );

            this.connection.start();
        }
        catch ( JMSException e )
        {
            LOG.error( "Error while activating options for appender named [" + this.name + "].", e, 0 );
        }
        catch ( NamingException e )
        {
            LOG.error( "Error while activating options for appender named [" + this.name + "].", e, 0 );
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Error while activating options for appender named [" + this.name + "].", e, 0 );
        }
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

    protected boolean checkEntryConditions()
    {
        String fail = null;

        if ( this.connection == null )
            fail = "No TopicConnection";
        else if ( this.session == null )
            fail = "No TopicSession";
        else if ( this.producer == null )
        {
            fail = "No TopicPublisher";
        }

        if ( fail != null )
        {
            LOG.error( fail + " for JMSAppender named [" + this.name + "]." );
            return false;
        }
        return true;
    }

    public synchronized void close()
    {
        if ( !( this.started ) )
            return;
        this.started = false;
        try
        {
            if ( this.session != null )
                this.session.close();
            if ( this.connection != null )
                this.connection.close();
        }
        catch ( JMSException e )
        {
            LOG.error( "Error while closing JMSAppender [" + this.name + "].", e );
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Error while closing JMSAppender [" + this.name + "].", e );
        }
        catch ( Exception e )
        {
            LOG.error( "Error while closing JMSAppender [" + this.name + "].", e );
        }

        this.producer = null;
        this.session = null;
        this.connection = null;
    }

    public void append( LoggingEvent event )
    {
        if ( !checkEntryConditions() )
        {
            return;
        }
        try
        {
            ObjectMessage msg = this.session.createObjectMessage();

            Object message = event.getMessage();
            msg.setObject( (Serializable) message );

            this.producer.send( msg );
        }
        catch ( JMSException e )
        {
            LOG.error( "Could not publish message in JMSAppender [" + this.name + "].", e, 0 );
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Could not publish message in JMSAppender [" + this.name + "].", e, 0 );
        }
    }

    public String getInitialContextFactoryName()
    {
        return this.initialContextFactoryName;
    }

    public void setInitialContextFactoryName( String initialContextFactoryName )
    {
        this.initialContextFactoryName = initialContextFactoryName;
    }

    public String getProviderURL()
    {
        return this.providerURL;
    }

    public void setProviderURL( String providerURL )
    {
        this.providerURL = providerURL;
    }

    String getURLPkgPrefixes()
    {
        return this.urlPkgPrefixes;
    }

    public void setURLPkgPrefixes( String urlPkgPrefixes )
    {
        this.urlPkgPrefixes = urlPkgPrefixes;
    }

    public String getSecurityCredentials()
    {
        return this.securityCredentials;
    }

    public void setSecurityCredentials( String securityCredentials )
    {
        this.securityCredentials = securityCredentials;
    }

    public String getSecurityPrincipalName()
    {
        return this.securityPrincipalName;
    }

    public void setSecurityPrincipalName( String securityPrincipalName )
    {
        this.securityPrincipalName = securityPrincipalName;
    }

    public String getUserName()
    {
        return this.userName;
    }

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public void setLocationInfo( boolean locationInfo )
    {
        this.locationInfo = locationInfo;
    }

    protected Connection getTopicConnection()
    {
        return this.connection;
    }

    protected Session getTopicSession()
    {
        return this.session;
    }

    protected MessageProducer getTopicPublisher()
    {
        return this.producer;
    }

    public boolean requiresLayout()
    {
        return false;
    }
}
