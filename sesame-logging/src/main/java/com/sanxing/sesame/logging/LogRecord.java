package com.sanxing.sesame.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LogRecord
    extends Throwable
{
    private static final long serialVersionUID = -3144056887643453625L;

    private static AtomicLong globalSequenceNumber = new AtomicLong( 0L );

    private static AtomicInteger nextThreadId = new AtomicInteger( 10 );

    private static final long defaultExpireInterval = 3600000L;

    private long sequenceNumber;

    private int threadID;

    private Object content;

    private Date timestamp;

    private String serviceName;

    private String operationName;

    private String channel;

    private String action;

    private String stage;

    private long expireTime;

    private static ThreadLocal<Object> threadIds = new ThreadLocal();

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName( String serviceName )
    {
        this.serviceName = serviceName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName( String operationName )
    {
        this.operationName = operationName;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel( String channel )
    {
        this.channel = channel;
    }

    public long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime( long expireTime )
    {
        this.expireTime = expireTime;
    }

    public String getStage()
    {
        return stage;
    }

    public void setStage( String stage )
    {
        this.stage = stage;
    }

    public LogRecord()
    {
        initialize();
    }

    protected LogRecord( Throwable throwable )
    {
        super( throwable );
        initialize();
    }

    private void initialize()
    {
        timestamp = new Date();

        sequenceNumber = globalSequenceNumber.incrementAndGet();
        Integer id = (Integer) threadIds.get();
        if ( id == null )
        {
            id = new Integer( nextThreadId.incrementAndGet() );
            threadIds.set( id );
        }
        threadID = id.intValue();
        expireTime = ( System.currentTimeMillis() + 3600000L );
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp( Date timestamp )
    {
        this.timestamp = timestamp;
    }

    public int getThreadID()
    {
        return threadID;
    }

    public void setSerial( long serial )
    {
        sequenceNumber = serial;
    }

    public long getSerial()
    {
        return sequenceNumber;
    }

    public void setAction( String action )
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }

    public void setContent( Object content )
    {
        this.content = content;
    }

    public Object getContent()
    {
        return content;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "{" );
        buf.append( " serial: " + getSerial() );
        if ( action != null )
        {
            buf.append( ", action: '" + action + "'" );
        }
        buf.append( " content: \"" );
        buf.append( content );
        buf.append( "\" " );
        buf.append( "}" );
        return buf.toString();
    }

    @Override
    public void printStackTrace()
    {
    }

    @Override
    public void printStackTrace( PrintStream s )
    {
        s.println( toString() );
    }

    @Override
    public void printStackTrace( PrintWriter writer )
    {
        writer.println( toString() );
    }
}