package com.sanxing.sesame.deployment;

public class ServiceAssembly
{
    private Connections connections;

    private Identification identification;

    private ServiceUnit[] serviceUnits;

    private String state;

    public ServiceAssembly()
    {
        connections = new Connections();

        state = "";
    }

    public Connections getConnections()
    {
        return connections;
    }

    public Identification getIdentification()
    {
        return identification;
    }

    public ServiceUnit[] getServiceUnits()
    {
        return serviceUnits;
    }

    public String getState()
    {
        return state;
    }

    public void setConnections( Connections connections )
    {
        this.connections = connections;
    }

    public void setIdentification( Identification identification )
    {
        this.identification = identification;
    }

    public void setServiceUnits( ServiceUnit[] serviceUnits )
    {
        this.serviceUnits = serviceUnits;
    }

    public void setState( String state )
    {
        this.state = state;
    }
}