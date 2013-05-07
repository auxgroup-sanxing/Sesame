package com.sanxing.sesame.binding.assist;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.PerfRecord;

public class BlockedWorker
    implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( BaseTransport.class );

    private final BlockedAcceptor acceptor;

    private final PipeLine pipeline;

    public BlockedWorker( BlockedAcceptor acceptor, PipeLine pipeline )
    {
        this.pipeline = pipeline;
        this.acceptor = acceptor;
    }

    @Override
    public void run()
    {
        try
        {
            Log sensor = LogFactory.getLog( "sesame.system.sensor" );

            boolean keepAlive = acceptor.getKeepAlive();
            do
            {
                long millis = System.currentTimeMillis();
                try
                {
                    int length = pipeline.extractHead();
                    byte[] inputBuf;
                    if ( length == -1 )
                    {
                        int size = acceptor.getBufferSize();
                        inputBuf = acceptor.allocate( size );
                        length = pipeline.read( inputBuf );
                    }
                    else
                    {
                        inputBuf = acceptor.allocate( length );
                        pipeline.read( inputBuf, 0, length );
                    }

                    if ( length < 1 )
                    {
                        break;
                    }

                    BinarySource source = new BinarySource();
                    source.setBytes( inputBuf, length );
                    source.setEncoding( acceptor.getCharacterEncoding() );
                    source.setProperty( "contextRoot", acceptor.getURI() );
                    BinaryResult result = acceptor.doSend( "/", source );

                    byte[] outputBuf = result.getBytes();

                    Boolean keepProp = (Boolean) result.getProperty( "keep-alive" );
                    if ( keepProp != null )
                    {
                        keepAlive = keepProp.booleanValue();
                    }

                    pipeline.write( outputBuf, 0, outputBuf.length );
                }
                catch ( SocketTimeoutException e )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( "Read request data timed out" );
                    }

                    break;
                }
                catch ( SocketException e )
                {
                    if ( LOG.isInfoEnabled() )
                    {
                        LOG.info( e.getMessage() );
                    }

                    break;
                }
                catch ( Throwable e )
                {
                    LOG.error( e.getMessage(), e );
                    String errorTip = ( e.getMessage() != null ) ? e.getMessage() : e.getClass().getName();
                    byte[] outputBuf = errorTip.getBytes( acceptor.getCharacterEncoding() );
                    pipeline.write( outputBuf, 0, outputBuf.length );

                    if ( sensor.isInfoEnabled() )
                    {
                        PerfRecord record = new PerfRecord();
                        record.setElapsedTime( System.currentTimeMillis() - millis );
                        record.setSerial( 0L );
                        sensor.info(
                            "[TOTAL TIME]--------------------------------------------------------------------", record );
                    }
                }
            }
            while ( keepAlive );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            try
            {
                pipeline.close();
            }
            catch ( IOException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
    }
}