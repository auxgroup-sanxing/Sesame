package com.sanxing.sesame.component;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.service.ServiceUnit;

public class UnitManagerProxy
    implements ServiceUnitManager
{
    private static Logger LOG = LoggerFactory.getLogger( UnitManagerProxy.class );

    private final Map<String, ServiceUnit> serviceUnits = new Hashtable();

    private final ComponentSupport component;

    private final ServiceUnitManager manager;

    public UnitManagerProxy( ComponentSupport component, ServiceUnitManager serviceUnitManager )
    {
        this.component = component;
        manager = serviceUnitManager;
    }

    @Override
    public String deploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return manager.deploy( serviceUnitName, serviceUnitRootPath );
    }

    @Override
    public String undeploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return manager.undeploy( serviceUnitName, serviceUnitRootPath );
    }

    @Override
    public void init( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        try
        {
            ServiceUnit serviceUnit = new ServiceUnit( new File( serviceUnitRootPath ) );
            serviceUnits.put( serviceUnitName, serviceUnit );
        }
        catch ( JBIException e )
        {
            if ( e.getCause() != null )
            {
                Throwable t = e.getCause();
                LOG.trace( t.getMessage(), t );
            }
            throw component.taskFailure( "init service unit " + serviceUnitName, e.getMessage() );
        }
        manager.init( serviceUnitName, serviceUnitRootPath );
    }

    @Override
    public void shutDown( String serviceUnitName )
        throws DeploymentException
    {
        manager.shutDown( serviceUnitName );
        serviceUnits.remove( serviceUnitName );
    }

    @Override
    public void start( String serviceUnitName )
        throws DeploymentException
    {
        manager.start( serviceUnitName );
    }

    @Override
    public void stop( String serviceUnitName )
        throws DeploymentException
    {
        manager.stop( serviceUnitName );
    }

    public ServiceUnit getServiceUnit( String serviceUnitName )
    {
        return serviceUnits.get( serviceUnitName );
    }

    public ServiceUnit getServiceUnit( QName serviceName )
    {
        for ( Object element : serviceUnits.values() )
        {
            ServiceUnit unit = (ServiceUnit) element;
            if ( serviceName.equals( unit.getServiceName() ) )
            {
                return unit;
            }
        }
        return null;
    }
}