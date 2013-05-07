package com.sanxing.sesame.mbean;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ComponentNameSpace
    implements Externalizable
{
    private static final long serialVersionUID = -9130913368962887486L;

    protected String containerName;

    protected String name;

    public ComponentNameSpace()
    {
    }

    public ComponentNameSpace( String containerName, String componentName )
    {
        this.containerName = containerName;
        name = componentName;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String componentName )
    {
        name = componentName;
    }

    public String getContainerName()
    {
        return containerName;
    }

    public void setContainerName( String containerName )
    {
        this.containerName = containerName;
    }

    @Override
    public boolean equals( Object obj )
    {
        boolean result = false;
        if ( obj instanceof ComponentNameSpace )
        {
            ComponentNameSpace other = (ComponentNameSpace) obj;
            result = ( other.containerName.equals( containerName ) ) && ( other.name.equals( name ) );
        }

        return result;
    }

    @Override
    public int hashCode()
    {
        return ( containerName.hashCode() ^ name.hashCode() );
    }

    @Override
    public String toString()
    {
        return "[container=" + containerName + ",name=" + name + "]";
    }

    @Override
    public void writeExternal( ObjectOutput out )
        throws IOException
    {
        out.writeUTF( ( containerName != null ) ? containerName : "" );
        out.writeUTF( ( name != null ) ? name : "" );
    }

    @Override
    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        containerName = in.readUTF();
        name = in.readUTF();
    }

    public ComponentNameSpace copy()
    {
        return new ComponentNameSpace( containerName, name );
    }
}