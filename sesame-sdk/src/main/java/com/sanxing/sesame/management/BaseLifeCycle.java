package com.sanxing.sesame.management;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

public abstract class BaseLifeCycle
    implements LifeCycleMBean, MBeanInfoProvider
{
    public static final String INITIALIZED = "Initialized";

    protected String currentState = "Unknown";

    protected PropertyChangeListener listener;

    @Override
    public String getName()
    {
        String name = super.getClass().getName();
        int index = name.lastIndexOf( "." );
        if ( ( index >= 0 ) && ( index + 1 < name.length() ) )
        {
            name = name.substring( index + 1 );
        }
        return name;
    }

    @Override
    public String getType()
    {
        String name = super.getClass().getName();
        int index = name.lastIndexOf( "." );
        if ( ( index >= 0 ) && ( index + 1 < name.length() ) )
        {
            name = name.substring( index + 1 );
        }
        return name;
    }

    @Override
    public String getSubType()
    {
        return null;
    }

    protected void init()
        throws JBIException
    {
        setCurrentState( "Initialized" );
    }

    @Override
    public void start()
        throws JBIException
    {
        setCurrentState( "Started" );
    }

    @Override
    public void stop()
        throws JBIException
    {
        setCurrentState( "Stopped" );
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        setCurrentState( "Shutdown" );
    }

    @Override
    public String getCurrentState()
    {
        return currentState;
    }

    protected void setCurrentState( String newValue )
    {
        String oldValue = currentState;
        currentState = newValue;
        firePropertyChanged( "currentState", oldValue, newValue );
    }

    public boolean isStarted()
    {
        return ( ( currentState != null ) && ( currentState.equals( "Started" ) ) );
    }

    public boolean isStopped()
    {
        return ( ( currentState != null ) && ( currentState.equals( "Stopped" ) ) );
    }

    public boolean isShutDown()
    {
        return ( ( currentState != null ) && ( currentState.equals( "Shutdown" ) ) );
    }

    public boolean isInitialized()
    {
        return ( ( currentState != null ) && ( currentState.equals( "Initialized" ) ) );
    }

    public boolean isUnknown()
    {
        return ( ( currentState == null ) || ( currentState.equals( "Unknown" ) ) );
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "currentState", "Current State of Managed Item" );
        helper.addAttribute( getObjectToManage(), "name", "name of the Item" );
        helper.addAttribute( getObjectToManage(), "description", "description of the Item" );
        return helper.getAttributeInfos();
    }

    @Override
    public MBeanOperationInfo[] getOperationInfos()
        throws JMException
    {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation( getObjectToManage(), "start", "start the item" );
        helper.addOperation( getObjectToManage(), "stop", "stop the item" );
        helper.addOperation( getObjectToManage(), "shutDown", "shutdown the item" );
        return helper.getOperationInfos();
    }

    @Override
    public Object getObjectToManage()
    {
        return this;
    }

    @Override
    public void setPropertyChangeListener( PropertyChangeListener listener )
    {
        this.listener = listener;
    }

    protected void firePropertyChanged( String name, Object oldValue, Object newValue )
    {
        if ( listener != null )
        {
            PropertyChangeEvent event = new PropertyChangeEvent( this, name, oldValue, newValue );
            listener.propertyChange( event );
        }
    }
}