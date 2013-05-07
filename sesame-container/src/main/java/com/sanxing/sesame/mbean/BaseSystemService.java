package com.sanxing.sesame.mbean;

import javax.jbi.JBIException;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.BaseLifeCycle;

public abstract class BaseSystemService
    extends BaseLifeCycle
{
    protected JBIContainer container;

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
        return "SystemService";
    }

    public void init( JBIContainer cont )
        throws JBIException
    {
        container = cont;
        cont.getManagementContext().registerSystemService( this, getServiceMBean() );
        super.init();
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        stop();
        super.shutDown();
        if ( ( container != null ) && ( container.getManagementContext() != null ) )
        {
            container.getManagementContext().unregisterSystemService( this );
        }
    }

    protected abstract Class getServiceMBean();

    public JBIContainer getContainer()
    {
        return container;
    }
}