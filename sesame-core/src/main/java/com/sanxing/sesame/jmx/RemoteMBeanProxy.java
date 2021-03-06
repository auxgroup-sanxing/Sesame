package com.sanxing.sesame.jmx;

import java.io.IOException;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

public class RemoteMBeanProxy
    implements DynamicMBean, NotificationEmitter, MBeanRegistration
{
    private final ObjectName remoteObjectName;

    private final JMXConnector connector;

    private final MBeanServerConnection connection;

    public RemoteMBeanProxy( ObjectName remoteObjectName, JMXServiceURL url, Map environment, Subject delegate )
        throws IOException
    {
        this( remoteObjectName, JMXConnectorFactory.newJMXConnector( url, environment ), environment, delegate );
    }

    public RemoteMBeanProxy( ObjectName remoteObjectName, JMXConnector connector, Map environment, Subject delegate )
        throws IOException
    {
        this.remoteObjectName = remoteObjectName;
        this.connector = connector;
        this.connector.connect( environment );
        connection = connector.getMBeanServerConnection( delegate );
    }

    public RemoteMBeanProxy( ObjectName remoteObjectName, MBeanServerConnection connection )
    {
        this.remoteObjectName = remoteObjectName;
        connector = null;
        this.connection = connection;
    }

    @Override
    public ObjectName preRegister( MBeanServer server, ObjectName name )
        throws Exception
    {
        return name;
    }

    @Override
    public void postRegister( Boolean registrationDone )
    {
    }

    @Override
    public void preDeregister()
        throws Exception
    {
        JMXConnector cntor = getJMXConnector();
        if ( cntor == null )
        {
            return;
        }
        cntor.close();
    }

    @Override
    public void postDeregister()
    {
    }

    protected ObjectName getRemoteObjectName()
    {
        return remoteObjectName;
    }

    protected MBeanServerConnection getMBeanServerConnection()
    {
        return connection;
    }

    protected JMXConnector getJMXConnector()
    {
        return connector;
    }

    @Override
    public MBeanInfo getMBeanInfo()
    {
        try
        {
            return getMBeanServerConnection().getMBeanInfo( getRemoteObjectName() );
        }
        catch ( Exception x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public Object getAttribute( String attribute )
        throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        try
        {
            return getMBeanServerConnection().getAttribute( getRemoteObjectName(), attribute );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public void setAttribute( Attribute attribute )
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        try
        {
            getMBeanServerConnection().setAttribute( getRemoteObjectName(), attribute );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public AttributeList getAttributes( String[] attributes )
    {
        try
        {
            return getMBeanServerConnection().getAttributes( getRemoteObjectName(), attributes );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( ReflectionException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public AttributeList setAttributes( AttributeList attributes )
    {
        try
        {
            return getMBeanServerConnection().setAttributes( getRemoteObjectName(), attributes );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( ReflectionException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public Object invoke( String method, Object[] arguments, String[] params )
        throws MBeanException, ReflectionException
    {
        try
        {
            return getMBeanServerConnection().invoke( getRemoteObjectName(), method, arguments, params );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo()
    {
        return getMBeanInfo().getNotifications();
    }

    @Override
    public void addNotificationListener( NotificationListener listener, NotificationFilter filter, Object handback )
        throws IllegalArgumentException
    {
        try
        {
            getMBeanServerConnection().addNotificationListener( getRemoteObjectName(), listener, filter, handback );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public void removeNotificationListener( NotificationListener listener )
        throws ListenerNotFoundException
    {
        try
        {
            getMBeanServerConnection().removeNotificationListener( getRemoteObjectName(), listener );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }

    @Override
    public void removeNotificationListener( NotificationListener listener, NotificationFilter filter, Object handback )
        throws ListenerNotFoundException
    {
        try
        {
            getMBeanServerConnection().removeNotificationListener( getRemoteObjectName(), listener, filter, handback );
        }
        catch ( InstanceNotFoundException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
        catch ( IOException x )
        {
            throw new RemoteMBeanProxyException( x );
        }
    }
}