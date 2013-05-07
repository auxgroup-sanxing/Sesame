package com.sanxing.sesame.deployment;

public class Descriptor
{
    private double version;

    private Component component;

    private SharedLibrary sharedLibrary;

    private ServiceAssembly serviceAssembly;

    private Services services;

    public double getVersion()
    {
        return version;
    }

    public void setVersion( double version )
    {
        this.version = version;
    }

    public Component getComponent()
    {
        return component;
    }

    public void setComponent( Component component )
    {
        this.component = component;
    }

    public SharedLibrary getSharedLibrary()
    {
        return sharedLibrary;
    }

    public void setSharedLibrary( SharedLibrary sharedLibrary )
    {
        this.sharedLibrary = sharedLibrary;
    }

    public ServiceAssembly getServiceAssembly()
    {
        return serviceAssembly;
    }

    public void setServiceAssembly( ServiceAssembly serviceAssembly )
    {
        this.serviceAssembly = serviceAssembly;
    }

    public Services getServices()
    {
        return services;
    }

    public void setServices( Services services )
    {
        this.services = services;
    }
}