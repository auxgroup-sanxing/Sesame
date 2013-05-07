package com.sanxing.sesame.logging;

public class PerfRecord
    extends LogRecord
{
    private long elapsedTime;

    private static final long serialVersionUID = 6133358861123651217L;

    public void setElapsedTime( long elapsedTime )
    {
        this.elapsedTime = elapsedTime;
    }

    public long getElapsedTime()
    {
        return elapsedTime;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "{" );
        buf.append( " serial: " + getSerial() );
        if ( getServiceName() != null )
        {
            buf.append( ", serviceUnit: '" + getServiceName() + "'" );
        }
        if ( getOperationName() != null )
        {
            buf.append( ", operation: '" + getOperationName() + "'" );
        }
        if ( getAction() != null )
        {
            buf.append( ", action: '" + getAction() + "'" );
        }
        buf.append( ", elapsedTime: " + elapsedTime );
        if ( getContent() != null )
        {
            buf.append( ", content: " + getContent() );
        }
        buf.append( "}" );
        return buf.toString();
    }
}