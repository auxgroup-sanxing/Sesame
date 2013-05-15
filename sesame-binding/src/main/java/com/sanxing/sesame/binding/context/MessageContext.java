package com.sanxing.sesame.binding.context;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import com.sanxing.sesame.binding.Binding;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.serial.SerialGenerator;

public class MessageContext
{
    private Source input;

    private Result output;

    private Exception exception;

    private boolean closed;

    private Transport transport;

    private long serial;

    private String path;

    private String action;

    private Binding binding;

    private Status status;

    private Mode mode;

    private boolean accepted;

    private Map<String, Object> properties;

    private long timeout;

    public MessageContext( Acceptor acceptor, Source input )
    {
        this();
        serial = SerialGenerator.getSerial();
        setTransport( acceptor );
        setAccepted( true );
        this.input = input;
    }

    public MessageContext( Connector connector, Result output )
    {
        this();
        setTransport( connector );
        setAccepted( false );
        this.output = output;
    }

    protected MessageContext()
    {
        closed = false;

        path = "";
        action = null;

        status = Status.OK;
        mode = Mode.BLOCK;

        properties = new Hashtable();
    }

    public void setSerial( long serial )
        throws IllegalAccessException
    {
        if ( isAccepted() )
        {
            throw new IllegalAccessException( "Serial number has been set" );
        }
        this.serial = serial;
    }

    public Long getSerial()
    {
        return Long.valueOf( serial );
    }

    public void setSource( Source input )
    {
        this.input = input;
    }

    public Source getSource()
    {
        return input;
    }

    public void setResult( Result output )
    {
        this.output = output;
    }

    public Result getResult()
    {
        return output;
    }

    public Exception getException()
    {
        return exception;
    }

    public void close()
        throws IOException
    {
        closed = true;
    }

    public boolean isClosed()
    {
        return closed;
    }

    public Transport getTransport()
    {
        return transport;
    }

    public void setTransport( Transport transport )
    {
        this.transport = transport;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction( String action )
    {
        this.action = action;
    }

    public void setException( Exception exception )
    {
        this.exception = exception;
    }

    public void setStatus( Status status )
    {
        this.status = status;
    }

    public boolean isAccepted()
    {
        return accepted;
    }

    protected void setAccepted( boolean accepted )
    {
        this.accepted = accepted;
    }

    public Status getStatus()
    {
        return status;
    }

    public Mode getMode()
    {
        return mode;
    }

    public void setMode( Mode mode )
    {
        this.mode = mode;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout( long timeout )
    {
        this.timeout = timeout;
    }

    public Binding getBinding()
    {
        return binding;
    }

    public void setBinding( Binding binding )
    {
        this.binding = binding;
    }

    public Object getProperty( String name )
    {
        return properties.get( name );
    }

    public void setProperty( String name, Object value )
    {
        properties.put( name, value );
    }

    public Set<String> getPropertyNames()
    {
        return properties.keySet();
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "{ class: '" + super.getClass().getSimpleName() + "'" );
        buf.append( ", serial: " + serial );
        buf.append( ", action: " + ( ( action != null ) ? "'" + action + "'" : "null" ) );
        buf.append( ", contextPath: '" + path + "'" );
        buf.append( ", mode: '" + mode + "'" );
        if ( mode == Mode.BLOCK )
        {
            buf.append( ", timeout: " + timeout + "ms" );
        }
        buf.append( ", status: '" + status + "'" );
        if ( input != null )
        {
            buf.append( ", input: " + input );
        }
        if ( output != null )
        {
            buf.append( ", output: " + output );
        }
        buf.append( " }" );
        return buf.toString();
    }

    public static class Mode
    {
        public static final Mode BLOCK = new Mode( "block" );

        public static final Mode NON_BLOCK = new Mode( "non-block" );

        private final String code;

        private Mode( String code )
        {
            this.code = code;
        }

        @Override
        public String toString()
        {
            return code;
        }
    }

    public static class Status
    {
        public static final Status OK = new Status( "ok" );

        public static final Status FAULT = new Status( "fault" );

        private final String code;

        private Status( String code )
        {
            this.code = code;
        }

        @Override
        public String toString()
        {
            return code;
        }
    }
}