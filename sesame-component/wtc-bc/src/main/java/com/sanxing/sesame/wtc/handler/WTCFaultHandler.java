package com.sanxing.sesame.wtc.handler;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.FaultHandler;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.binding.codec.XMLSource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.exceptions.SystemException;
import com.sanxing.sesame.wtc.WTCDecoder;
import com.sanxing.sesame.wtc.WTCEncoder;
import com.sanxing.sesame.wtc.output.SOPOutputter;
import com.sanxing.sesame.wtc.sop.SOPResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WTCFaultHandler
    implements FaultHandler
{
    private static final Logger LOG = LoggerFactory.getLogger( WTCFaultHandler.class );

    public void encode( XMLSource source, BinaryResult result )
    {
        try
        {
            LOG.debug( "WTC:encode fault message: " );
            new WTCEncoder().encode( source, result );
            LOG.debug( "WTC:encode fault message finish." );
            LOG.debug( SOPOutputter.format( result.getBytes() ) );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    public void handle( Exception e, MessageContext context )
    {
        try
        {
            byte[] bytes = errResponse( e, context );

            BinaryResult result = (BinaryResult) context.getResult();
            OutputStream output = new ByteArrayOutputStream();
            result.setOutputStream( output );
            output.write( bytes );
        }
        catch ( IOException e1 )
        {
            e1.printStackTrace();
        }
    }

    public void decode( BinarySource source, XMLResult result )
    {
        try
        {
            LOG.debug( "WTC:decode fault message: " );
            LOG.debug( SOPOutputter.format( source.getBytes() ) );
            new WTCDecoder().decode( source, result );
            LOG.debug( "WTC:decode fault message finish." );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    private byte[] errResponse( Exception e, MessageContext context )
    {
        String tranCode = null;
        Source source = context.getSource();

        if ( ( source instanceof XMLSource ) )
        {
            tranCode = (String) ( (XMLSource) source ).getProperty( "tranCode" );
        }
        else if ( ( source instanceof BinarySource ) )
        {
            tranCode = (String) ( (BinarySource) source ).getProperty( "tranCode" );
        }

        String errorCode = null;
        String errorMsg = null;
        if ( ( e instanceof AppException ) )
        {
            errorCode = ( (AppException) e ).getErrorCode();
            errorMsg = e.getMessage();
        }
        else if ( ( e instanceof SystemException ) )
        {
            errorCode = ( (SystemException) e ).getErrorCode();
            errorMsg = e.getMessage();
        }
        else if ( ( e instanceof RuntimeException ) )
        {
            String msg = e.getMessage();
            int index = msg.lastIndexOf( "|" );
            errorCode = msg.substring( 0, index );
            errorMsg = msg.substring( index + 1 );
        }

        String serial = String.valueOf( context.getSerial() );
        SOPResponse resp = new SOPResponse( errorCode, errorMsg );
        resp.setTranCode( tranCode );
        resp.setSerial( serial );
        byte[] content = resp.encodeBody();
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "WTC:send fault message: " );
            LOG.debug( SOPOutputter.format( content ) );
        }
        return content;
    }
}
