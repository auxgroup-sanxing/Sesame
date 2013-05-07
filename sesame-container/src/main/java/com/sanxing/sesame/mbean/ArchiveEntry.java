package com.sanxing.sesame.mbean;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class ArchiveEntry
    implements Serializable
{
    protected String location;

    protected Date lastModified;

    protected String type;

    protected String name;

    protected boolean pending;

    protected transient Set<String> dependencies;

    public String getLocation()
    {
        return location;
    }

    public void setLocation( String location )
    {
        this.location = location;
    }

    public Date getLastModified()
    {
        return lastModified;
    }

    public void setLastModified( Date lastModified )
    {
        this.lastModified = lastModified;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public boolean isPending()
    {
        return pending;
    }

    public void setPending( boolean pending )
    {
        this.pending = pending;
    }

    public Set<String> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies( Set<String> dependencies )
    {
        this.dependencies = dependencies;
    }

    @Override
    public String toString()
    {
        return "ArchiveEntry [lastModified=" + lastModified + ", location=" + location + ", name=" + name
            + ", pending=" + pending + ", type=" + type + "]";
    }
}