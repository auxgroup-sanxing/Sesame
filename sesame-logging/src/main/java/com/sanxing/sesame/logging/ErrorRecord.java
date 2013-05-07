package com.sanxing.sesame.logging;

import com.sanxing.sesame.logging.constants.LogStage;

public class ErrorRecord
    extends LogRecord
{
    private static final long serialVersionUID = 4547137137339015960L;

    private Exception exception;

    public ErrorRecord( long serial, Exception exception )
    {
        super( exception );
        setException( exception );
        setSerial( serial );
        setStage( LogStage.STAGE_END );
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException( Exception exception )
    {
        this.exception = exception;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "{" );
        buf.append( " serial: " + getSerial() );
        if ( getAction() != null )
        {
            buf.append( ", action: '" + getAction() + "'" );
        }
        buf.append( ", status: error" );
        if ( getCause() != null )
        {
            buf.append( ", exception: " + getCause() );
        }
        buf.append( "}" );
        return buf.toString();
    }
}