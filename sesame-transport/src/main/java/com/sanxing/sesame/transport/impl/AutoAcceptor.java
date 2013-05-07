package com.sanxing.sesame.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;

public class AutoAcceptor
    extends TaskTransport
{
    private static Logger LOG = LoggerFactory.getLogger( AutoAcceptor.class );

    @Override
    public void executeTask( Map<?, ?> properties )
        throws Exception
    {
        LOG.debug( "jobName is:" + properties.get( "jobName" ) );
        accept( new ByteArrayInputStream( "".getBytes() ), "getfile", (String) properties.get( "jobName" ) );
    }

    @Override
    public void reply( MessageContext context )
        throws BindingException, IOException
    {
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
        catch ( SchedulerException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public String getCharacterEncoding()
    {
        return encoding;
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
        catch ( SchedulerException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }
}