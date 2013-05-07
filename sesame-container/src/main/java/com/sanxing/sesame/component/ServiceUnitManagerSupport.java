package com.sanxing.sesame.component;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;

public class ServiceUnitManagerSupport
    implements ServiceUnitManager
{
    @Override
    public String deploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return "<component-task-result>" + serviceUnitRootPath + "</component-task-result>";
    }

    @Override
    public void init( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
    }

    @Override
    public void shutDown( String serviceUnitName )
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
    public String undeploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return serviceUnitRootPath;
    }
}