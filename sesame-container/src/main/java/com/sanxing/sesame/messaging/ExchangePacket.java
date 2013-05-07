package com.sanxing.sesame.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Transaction;
import javax.xml.namespace.QName;

import com.sanxing.sesame.component.CopyTransformer;
import com.sanxing.sesame.mbean.ComponentNameSpace;

public class ExchangePacket
    implements Externalizable
{
    private static final long serialVersionUID = -9110837382914609624L;

    protected URI pattern;

    protected String exchangeId;

    protected ComponentNameSpace destinationId;

    protected ComponentNameSpace sourceId;

    protected ExchangeStatus status = ExchangeStatus.ACTIVE;

    protected QName serviceName;

    protected QName interfaceName;

    protected QName operationName;

    protected Exception error;

    protected Map properties;

    protected NormalizedMessageImpl in;

    protected NormalizedMessageImpl out;

    protected FaultImpl fault;

    protected ServiceEndpoint endpoint;

    protected transient Transaction transactionContext;

    protected Boolean persistent;

    protected boolean aborted;

    public ExchangePacket()
    {
    }

    public ExchangePacket( ExchangePacket packet )
        throws MessagingException
    {
        destinationId = packet.destinationId;
        endpoint = null;
        error = null;
        exchangeId = null;
        interfaceName = packet.interfaceName;
        CopyTransformer ct = new CopyTransformer();
        if ( packet.in != null )
        {
            in = new NormalizedMessageImpl();
            ct.transform( null, packet.in, in );
        }
        if ( packet.out != null )
        {
            out = new NormalizedMessageImpl();
            ct.transform( null, packet.out, out );
        }
        if ( packet.fault != null )
        {
            fault = new FaultImpl();
            ct.transform( null, packet.fault, fault );
        }
        operationName = packet.operationName;
        pattern = packet.pattern;
        if ( ( packet.properties != null ) && ( packet.properties.size() > 0 ) )
        {
            getProperties().putAll( packet.properties );
        }
        serviceName = packet.serviceName;
        sourceId = packet.sourceId;
        status = packet.status;
        transactionContext = packet.transactionContext;
        persistent = packet.persistent;
    }

    public ServiceEndpoint getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint( ServiceEndpoint endpoint )
    {
        this.endpoint = endpoint;
    }

    public Transaction getTransactionContext()
    {
        return transactionContext;
    }

    public void setTransactionContext( Transaction transactionContext )
    {
        this.transactionContext = transactionContext;
    }

    public QName getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName( QName interfaceName )
    {
        this.interfaceName = interfaceName;
    }

    public QName getOperationName()
    {
        return operationName;
    }

    public void setOperationName( QName operationName )
    {
        this.operationName = operationName;
    }

    public QName getServiceName()
    {
        return serviceName;
    }

    public void setServiceName( QName serviceName )
    {
        this.serviceName = serviceName;
    }

    public void setStatus( ExchangeStatus status )
    {
        this.status = status;
    }

    public ExchangeStatus getStatus()
    {
        return status;
    }

    public URI getPattern()
    {
        return pattern;
    }

    public void setPattern( URI pattern )
    {
        this.pattern = pattern;
    }

    public Exception getError()
    {
        return error;
    }

    public void setError( Exception error )
    {
        this.error = error;
        status = ExchangeStatus.ERROR;
    }

    public String getExchangeId()
    {
        return exchangeId;
    }

    public void setExchangeId( String exchangeId )
    {
        this.exchangeId = exchangeId;
    }

    public Map getProperties()
    {
        if ( properties == null )
        {
            properties = new HashMap();
        }
        return properties;
    }

    public Object getProperty( String name )
    {
        if ( properties != null )
        {
            return properties.get( name );
        }
        return null;
    }

    public void setProperty( String name, Object value )
    {
        if ( value == null )
        {
            if ( properties != null )
            {
                properties.remove( name );
            }
        }
        else
        {
            getProperties().put( name, value );
        }
    }

    public Set getPropertyNames()
    {
        if ( properties != null )
        {
            return Collections.unmodifiableSet( properties.keySet() );
        }
        return Collections.EMPTY_SET;
    }

    public ComponentNameSpace getSourceId()
    {
        return sourceId;
    }

    public void setSourceId( ComponentNameSpace sourceId )
    {
        this.sourceId = sourceId;
    }

    public ComponentNameSpace getDestinationId()
    {
        return destinationId;
    }

    public void setDestinationId( ComponentNameSpace destinationId )
    {
        this.destinationId = destinationId;
    }

    public Fault getFault()
    {
        return fault;
    }

    public void setFault( FaultImpl fault )
    {
        this.fault = fault;
    }

    public NormalizedMessage getIn()
    {
        return in;
    }

    public void setIn( NormalizedMessageImpl in )
    {
        this.in = in;
    }

    public NormalizedMessage getOut()
    {
        return out;
    }

    public void setOut( NormalizedMessageImpl out )
    {
        this.out = out;
    }

    @Override
    public String toString()
    {
        return "ExchangePacket[: id=" + exchangeId + ", serviceDest=" + serviceName + ",endpoint=" + endpoint + "]";
    }

    @Override
    public void writeExternal( ObjectOutput output )
        throws IOException
    {
        output.writeUTF( pattern.toString() );
        output.writeUTF( ( exchangeId != null ) ? exchangeId : "" );
        output.writeUTF( status.toString() );
        output.writeObject( destinationId );
        output.writeObject( sourceId );
        output.writeObject( serviceName );
        output.writeObject( interfaceName );
        output.writeObject( operationName );
        output.writeObject( error );
        output.writeObject( properties );
        output.writeObject( in );
        output.writeObject( out );
        output.writeObject( fault );
        output.writeObject( endpoint );
        output.writeByte( persistent == null ? 0 : ( persistent.booleanValue() ? 1 : 2 ) );
    }

    @Override
    public void readExternal( ObjectInput input )
        throws IOException, ClassNotFoundException
    {
        pattern = URI.create( input.readUTF() );
        exchangeId = input.readUTF();
        status = ExchangeStatus.valueOf( input.readUTF() );
        destinationId = ( (ComponentNameSpace) input.readObject() );
        sourceId = ( (ComponentNameSpace) input.readObject() );
        serviceName = ( (QName) input.readObject() );
        interfaceName = ( (QName) input.readObject() );
        operationName = ( (QName) input.readObject() );
        error = ( (Exception) input.readObject() );
        properties = ( (Map) input.readObject() );
        in = ( (NormalizedMessageImpl) input.readObject() );
        out = ( (NormalizedMessageImpl) input.readObject() );
        fault = ( (FaultImpl) input.readObject() );
        endpoint = ( (ServiceEndpoint) input.readObject() );
        byte p = input.readByte();
        persistent = ( ( p == 1 ) ? Boolean.TRUE : ( p == 0 ) ? null : Boolean.FALSE );
    }

    public ExchangePacket copy()
        throws MessagingException
    {
        return new ExchangePacket( this );
    }

    public Boolean getPersistent()
    {
        return persistent;
    }

    public void setPersistent( Boolean persistent )
    {
        this.persistent = persistent;
    }

    public boolean isAborted()
    {
        return aborted;
    }

    public void setAborted( boolean timedOut )
    {
        aborted = timedOut;
    }

    public byte[] getData()
        throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream( buffer );
        os.writeObject( this );
        os.close();
        return buffer.toByteArray();
    }

    public static ExchangePacket readPacket( byte[] data )
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( data ) );
        return ( (ExchangePacket) ois.readObject() );
    }
}