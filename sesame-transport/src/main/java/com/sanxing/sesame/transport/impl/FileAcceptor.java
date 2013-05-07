package com.sanxing.sesame.transport.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.util.SesameFileClient;

public class FileAcceptor
    extends TaskTransport
{
    private static Logger LOG = LoggerFactory.getLogger( FileAcceptor.class );

    protected void write( OutputStream output, byte[] bytes, int length )
        throws IOException
    {
    }

    @Override
    public String getCharacterEncoding()
    {
        return "utf-8";
    }

    public String getDescription()
    {
        return "mail transport";
    }

    @Override
    public void reply( MessageContext context )
        throws IOException
    {
        String path = context.getPath();
        Map props = bindingPropsCache.get( path );
        SesameFileClient fileClient = new SesameFileClient();
        fileClient.setBindingProperties( props );
        BinaryResult result = (BinaryResult) context.getResult();
        byte[] resp = result.getBytes();
        LOG.debug( "get response is:" + new String( resp ) );
        try
        {
            if ( resp != null )
            {
                fileClient.writeOneFile( resp );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void close()
        throws IOException
    {
        try
        {
            scheduler.shutdown();
            workExecutor.shutdown();
            active = false;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void open()
        throws IOException
    {
        try
        {
            active = true;
            scheduler = new SesameScheduler();
            scheduler.init();
            scheduler.start();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void executeTask( Map<?, ?> properties )
        throws Exception
    {
        SesameFileClient fileClient = new SesameFileClient();
        fileClient.setBindingProperties( properties );
        File[] files = fileClient.getFiles();

        if ( files != null )
        {
            for ( File file : files )
            {
                if ( fileClient.bakFile( file ) )
                {
                    InputStream in = fileClient.readOneFileWithName( file.getName() );
                    accept( in, file.getName(), (String) properties.get( "jobName" ) );
                }
                else
                {
                    LOG.error( "move file:[" + file.getName() + "] error!" );
                }
            }
        }
    }
}