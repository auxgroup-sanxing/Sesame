package com.sanxing.sesame.transport.quartz;

import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.transport.impl.TaskTransport;

public class TaskImpl
    implements StatefulJob
{
    private static Logger LOG = LoggerFactory.getLogger( TaskImpl.class );

    @Override
    public void execute( JobExecutionContext context )
        throws JobExecutionException
    {
        try
        {
            JobDetail detail = context.getJobDetail();
            JobDataMap map = detail.getJobDataMap();

            Map properties = (Map) map.get( "properties" );

            LOG.debug( "\n jobName is:[" + detail.getName() + "],thread name is:" + Thread.currentThread().getName()
                + "\n unit properties is:" + properties + "\n" );

            TaskTransport transport = (TaskTransport) properties.get( "transport" );
            if ( transport != null )
            {
                transport.executeTask( properties );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}