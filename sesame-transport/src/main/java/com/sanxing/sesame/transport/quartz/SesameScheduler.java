package com.sanxing.sesame.transport.quartz;

import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameScheduler
{
    private static Logger LOG = LoggerFactory.getLogger( SesameScheduler.class );

    private Scheduler scheduler;

    public void deleteJob( String jobName, String groupName )
        throws SchedulerException
    {
        scheduler.deleteJob( jobName, groupName );
    }

    public void removeTrigger( String jobName, String groupName, String timeExp )
        throws SchedulerException
    {
        scheduler.unscheduleJob( jobName + "_" + timeExp, groupName );
    }

    public void init()
        throws SchedulerException
    {
        SchedulerFactory factory = new StdSchedulerFactory();
        scheduler = factory.getScheduler();
    }

    public void modifyTrigger( String jobName, String groupName, String newExp, String oldExp )
        throws SchedulerException, Exception
    {
        Trigger oldTrigger = scheduler.getTrigger( jobName + "_" + oldExp, null );
        if ( oldTrigger == null )
        {
            throw new Exception( "job:[" + jobName + "],can not find the trigger cronExpression:[" + oldExp + "]" );
        }
        Trigger trigger = getTrigger( jobName, groupName, newExp );
        trigger.setJobName( jobName );
        scheduler.rescheduleJob( jobName + "_" + oldExp, groupName, trigger );
    }

    public void shutdown()
        throws SchedulerException
    {
        String[] jobGroupNames = scheduler.getJobGroupNames();

        if ( jobGroupNames.length == 0 )
        {
            scheduler.shutdown( true );
        }
    }

    public void shutdown( String groupName )
    {
    }

    public void start()
        throws SchedulerException
    {
        scheduler.start();
    }

    public void resumeJob( String jobName, String groupName )
        throws SchedulerException
    {
        scheduler.resumeJob( jobName, groupName );
    }

    public void stop()
        throws SchedulerException
    {
        if ( isStarted() )
        {
            scheduler.standby();
        }
    }

    public void pauseJob( String jobName, String groupName )
        throws SchedulerException
    {
        scheduler.pauseJob( jobName, groupName );
    }

    public void registryJob( String jobName, String groupName, Class<? extends Job> Job, String timeExp )
        throws Exception
    {
        scheduler.scheduleJob( new JobDetail( jobName, groupName, Job ), getTrigger( jobName, groupName, timeExp ) );
    }

    public void registryJob( Map<String, Object> props )
        throws Exception
    {
        String jobName = (String) props.get( "jobName" );
        String groupName = (String) props.get( "groupName" );
        String cornExp = (String) props.get( "cornExp" );
        Class taskClass = (Class) props.get( "taskClass" );

        JobDetail detail = new JobDetail( jobName, groupName, taskClass );
        JobDataMap dataMap = detail.getJobDataMap();
        dataMap.put( "properties", props );
        scheduler.scheduleJob( detail, getTrigger( jobName, groupName, cornExp ) );
    }

    public void registryJob( String jobName, String groupName, Class<? extends Job> job, String cornExp,
                             Map<String, Object> jobProps )
        throws Exception
    {
        JobDetail detail = new JobDetail( jobName, groupName, job );
        JobDataMap dataMap = detail.getJobDataMap();
        dataMap.put( "properties", jobProps );
        scheduler.scheduleJob( detail, getTrigger( jobName, groupName, cornExp ) );
    }

    public Trigger getTrigger( String jobName, String groupName, String cronExp )
        throws Exception
    {
        CronTrigger trigger = new CronTrigger( jobName + "_" + cronExp, groupName );
        trigger.setCronExpression( cronExp );

        trigger.setMisfireInstruction( 2 );
        return trigger;
    }

    public JobDetail getJobDetail( String jobName, Class<? extends Job> jobClass )
    {
        JobDetail detail = new JobDetail();
        detail.setJobClass( jobClass );
        detail.setName( jobName );
        return detail;
    }

    public static void main( String[] args )
        throws Exception
    {
        SesameScheduler impl = new SesameScheduler();

        SchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();
        JobDetail detail = new JobDetail( "/", "ftp", TaskImpl.class );
        scheduler.scheduleJob( detail, impl.getTrigger( "job1", "ftp", "0/2 * * * * ?" ) );

        JobDetail detail2 = new JobDetail( "/", "sftp", TaskImpl.class );
        scheduler.scheduleJob( detail2, impl.getTrigger( "job1", "mail", "0/2 * * * * ?" ) );
    }

    public void addTrigger( String jobName, String groupName, String timeExp )
        throws Exception
    {
        if ( scheduler.getTrigger( jobName + "_" + timeExp, groupName ) != null )
        {
            return;
        }
        CronTrigger trigger = new CronTrigger( jobName + "_" + timeExp, groupName );
        trigger.setJobName( jobName );
        trigger.setCronExpression( timeExp );
        trigger.setMisfireInstruction( 2 );
        scheduler.scheduleJob( trigger );
    }

    public void restart()
        throws SchedulerException
    {
        scheduler.resumeAll();
    }

    public boolean isStarted()
        throws SchedulerException
    {
        return scheduler.isStarted();
    }
}