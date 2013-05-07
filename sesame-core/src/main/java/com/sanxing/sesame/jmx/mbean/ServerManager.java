package com.sanxing.sesame.jmx.mbean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;

public class ServerManager
    implements ServerManagerMBean
{
    private static Logger LOG = LoggerFactory.getLogger( ServerManager.class );

    private final AtomicBoolean started = new AtomicBoolean( true );

    private BaseServer server = null;

    public ServerManager( BaseServer server )
    {
        this.server = server;
    }

    @Override
    public void listen( ClusterEvent event )
    {
        server.listenClusterEvent( event );
    }

    public void setServer( BaseServer server )
    {
        this.server = server;
    }

    @Override
    public void start()
    {
        if ( !( started.compareAndSet( false, true ) ) )
        {
            return;
        }
        try
        {
            server.start();
        }
        catch ( Exception e )
        {
            LOG.error( "start server " + server.getName() + " err", e );
            started.set( false );
            throw new RuntimeException( "start server error", e );
        }
    }

    @Override
    public void stop()
    {
        if ( !( started.compareAndSet( true, false ) ) )
        {
            return;
        }
        try
        {
            server.shutdown();
        }
        catch ( Exception e )
        {
            started.set( true );
            LOG.error( "stop server err " + server.getName(), e );
        }
    }

    @Override
    public String getName()
    {
        return server.getName();
    }

    @Override
    public String getHostAddress()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch ( UnknownHostException e )
        {
            return e.getMessage();
        }
    }

    @Override
    public int getJmxPort()
    {
        return Platform.getEnv().getAdminPort();
    }

    @Override
    public String getState()
    {
        String state = "unknown";
        switch ( server.getConfig().getServerState() )
        {
            case 1:
                state = "starting";
                break;
            case 2:
                state = "running";
                break;
            case 3:
                state = "stopping";
                break;
            case 0:
                state = "shutdown";
        }

        return state;
    }

    public void receiveEvent( ClusterEvent event )
    {
        server.listenClusterEvent( event );
    }

    @Override
    public String getDescription()
    {
        return "server manager";
    }

    @Override
    public String getSystemCpu()
    {
        String result = "0";
        JavaSysMon monitor = new JavaSysMon();
        CpuTimes preCpuTimes = monitor.cpuTimes();
        try
        {
            Thread.sleep( 500L );
        }
        catch ( InterruptedException localInterruptedException )
        {
        }
        float times = monitor.cpuTimes().getCpuUsage( preCpuTimes );
        result = String.format( "%.2f", new Object[] { Float.valueOf( times ) } );
        return result;
    }

    @Override
    public String getJVMMemory()
    {
        String result = "0";
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        float percent = 1.0F - ( (float) free / (float) total );
        result = String.format( "%.2f", new Object[] { Float.valueOf( percent ) } );
        return result;
    }
}