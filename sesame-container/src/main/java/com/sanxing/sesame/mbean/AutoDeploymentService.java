package com.sanxing.sesame.mbean;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.util.FileUtil;

public class AutoDeploymentService
    extends BaseSystemService
    implements AutoDeploymentServiceMBean
{
    private static final Logger LOG = LoggerFactory.getLogger( AutoDeploymentService.class );

    private EnvironmentContext environmentContext;

    private boolean monitorInstallationDirectory;

    private boolean monitorDeploymentDirectory;

    private int monitorInterval;

    private final AtomicBoolean started;

    private Timer statsTimer;

    private TimerTask timerTask;

    private ArchiveManager archiveManager;

    public AutoDeploymentService()
    {
        monitorInstallationDirectory = true;
        monitorDeploymentDirectory = true;
        monitorInterval = 10;

        started = new AtomicBoolean( false );
    }

    @Override
    public String getDescription()
    {
        return "automatically installs and deploys JBI Archives";
    }

    public boolean isMonitorInstallationDirectory()
    {
        return monitorInstallationDirectory;
    }

    public void setMonitorInstallationDirectory( boolean monitorInstallationDirectory )
    {
        this.monitorInstallationDirectory = monitorInstallationDirectory;
    }

    public boolean isMonitorDeploymentDirectory()
    {
        return monitorDeploymentDirectory;
    }

    public void setMonitorDeploymentDirectory( boolean monitorDeploymentDirectory )
    {
        this.monitorDeploymentDirectory = monitorDeploymentDirectory;
    }

    public int getMonitorInterval()
    {
        return monitorInterval;
    }

    public void setMonitorInterval( int monitorInterval )
    {
        this.monitorInterval = monitorInterval;
    }

    @Override
    public void start()
        throws JBIException
    {
        super.start();
        if ( started.compareAndSet( false, true ) )
        {
            scheduleDirectoryTimer();
        }
    }

    @Override
    public void stop()
        throws JBIException
    {
        if ( started.compareAndSet( true, false ) )
        {
            super.stop();
            if ( timerTask != null )
            {
                timerTask.cancel();
            }
        }
    }

    @Override
    public void init( JBIContainer container )
        throws JBIException
    {
        super.init( container );
        environmentContext = container.getEnvironmentContext();
        archiveManager = container.getArchiveManager();

        if ( environmentContext.getTmpDir() != null )
        {
            FileUtil.deleteFile( environmentContext.getTmpDir() );
        }
    }

    @Override
    protected Class<AutoDeploymentServiceMBean> getServiceMBean()
    {
        return AutoDeploymentServiceMBean.class;
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "monitorInstallationDirectory",
            "Periodically monitor the Installation directory" );
        helper.addAttribute( getObjectToManage(), "monitorInterval", "Interval (secs) before monitoring" );
        return AttributeInfoHelper.join( super.getAttributeInfos(), helper.getAttributeInfos() );
    }

    private void scheduleDirectoryTimer()
    {
        if ( ( isMonitorInstallationDirectory() ) || ( isMonitorDeploymentDirectory() ) )
        {
            if ( statsTimer == null )
            {
                statsTimer = new Timer( true );
            }
            if ( timerTask != null )
            {
                timerTask.cancel();
            }
            timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    if ( !( AutoDeploymentService.this.isStarted() ) )
                    {
                        return;
                    }
                    try
                    {
                        if ( AutoDeploymentService.this.isMonitorInstallationDirectory() )
                        {
                            archiveManager.scanInstallDir();
                        }
                        if ( AutoDeploymentService.this.isMonitorDeploymentDirectory() )
                        {
                            archiveManager.scanDeployDir();
                        }
                    }
                    catch ( Throwable t )
                    {
                        AutoDeploymentService.LOG.error( t.getMessage(), t );
                    }
                }
            };
            long interval = monitorInterval * 1000;
            statsTimer.scheduleAtFixedRate( timerTask, 0L, interval );
        }
    }
}