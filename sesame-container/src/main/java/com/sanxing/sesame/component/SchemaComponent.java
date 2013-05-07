package com.sanxing.sesame.component;

import java.io.File;
import java.io.IOException;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.w3c.dom.DocumentFragment;

public class SchemaComponent
    extends ComponentSupport
    implements ServiceUnitManager
{
    @Override
    public void init( ComponentContext componentContext )
        throws JBIException
    {
        super.init( componentContext );
    }

    @Override
    public boolean isBindingComponent()
    {
        return false;
    }

    @Override
    public boolean isEngineComponent()
    {
        return false;
    }

    @Override
    public String deploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        File unitFolder = new File( serviceUnitRootPath );
        File[] files = unitFolder.listFiles();
        try
        {
            for ( File file : files )
            {
                if ( !( file.renameTo( new File( unitFolder.getParentFile(), file.getName() ) ) ) )
                {
                    throw new IOException( "Failed to move " + file + " to " + unitFolder.getParent() );
                }
            }
        }
        catch ( IOException e )
        {
            throw taskFailure( "deploy", e.getMessage() );
        }
        return null;
    }

    @Override
    public String undeploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return null;
    }

    @Override
    public ServiceEndpoint resolveEndpointReference( DocumentFragment epr )
    {
        return null;
    }

    @Override
    public void init( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
    }

    @Override
    public void start( String serviceUnitName )
        throws DeploymentException
    {
    }

    @Override
    public void stop( String serviceUnitName )
        throws DeploymentException
    {
    }

    @Override
    public void shutDown( String serviceUnitName )
        throws DeploymentException
    {
    }

    @Override
    public ServiceUnitManager getServiceUnitManager()
    {
        return this;
    }

    @Override
    protected ServiceUnitManager createServiceUnitManager()
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return "Schema 管理组件";
    }
}