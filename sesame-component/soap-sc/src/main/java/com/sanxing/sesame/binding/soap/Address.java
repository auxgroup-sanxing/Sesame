package com.sanxing.sesame.binding.soap;

import java.io.Serializable;
import javax.xml.namespace.QName;

public class Address
    implements Serializable
{
    private static final long serialVersionUID = -4892325916646087564L;

    private String endpointName;

    private QName servcieName;

    private QName interfaceName;

    private QName operationName;

    public QName getServcieName()
    {
        return this.servcieName;
    }

    public void setServcieName( QName servcieName )
    {
        this.servcieName = servcieName;
    }

    public QName getInterfaceName()
    {
        return this.interfaceName;
    }

    public void setInterfaceName( QName _interfaceName )
    {
        this.interfaceName = _interfaceName;
    }

    public QName getOperationName()
    {
        return this.operationName;
    }

    public void setOperationName( QName _operationName )
    {
        this.operationName = _operationName;
    }

    public String getEndpointName()
    {
        return this.endpointName;
    }

    public void setEndpointName( String endpointName )
    {
        this.endpointName = endpointName;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( ( o == null ) || ( getClass() != o.getClass() ) )
        {
            return false;
        }
        Address address = (Address) o;

        if ( !this.endpointName.equals( address.endpointName ) )
            return false;
        if ( !this.interfaceName.equals( address.interfaceName ) )
            return false;
        if ( !this.operationName.equals( address.operationName ) )
            return false;
        if ( !this.servcieName.equals( address.servcieName ) )
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result = this.endpointName.hashCode();
        result = 31 * result + this.servcieName.hashCode();
        result = 31 * result + this.interfaceName.hashCode();
        result = 31 * result + this.operationName.hashCode();
        return result;
    }

    public String toString()
    {
        return "Address{servcieName=" + this.servcieName + ", interfaceName=" + this.interfaceName + ", operationName="
            + this.operationName + '}';
    }
}