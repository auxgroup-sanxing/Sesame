package com.sanxing.sesame.core;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Env
{
    public static final String MODE = "mode";

    public static final String ADMIN_HOST = "admin-host";

    public static final String ADMIN_PORT = "admin-port";

    public static final String SERVER_NAME = "server-name";

    private String mode;

    private String serverName;

    private String adminHost;

    private int adminPort;

    private boolean admin;

    private File serverDir;

    private String domain = "com.sanxing.sesame";

    private boolean clustered = false;

    public Env()
    {
        try
        {
            String strServerDir = System.getProperty( "SESAME_HOME" );
            if ( strServerDir == null )
            {
                strServerDir = new File( System.getProperty( "user.dir" ) ).getParent();
            }
            serverDir = new File( strServerDir );
            serverDir.mkdirs();

            mode = System.getProperty( "mode", "dev" );

            adminHost = System.getProperty( "admin-host" );

            InetAddress.getByName( adminHost );

            adminPort = Integer.parseInt( System.getProperty( "admin-port", "2099" ) );
            serverName = System.getProperty( "server-name", "admin" );
            admin = ( ( "admin".equalsIgnoreCase( getServerName() ) ) || ( "dev".equalsIgnoreCase( getMode() ) ) );
        }
        catch ( UnknownHostException e )
        {
            throw new RuntimeException( "Unkown admin host :" + adminHost );
        }
        catch ( NumberFormatException nfe )
        {
            throw new RuntimeException( "Unkown admin port :" + adminPort );
        }
    }

    public File getHomeDir()
    {
        return serverDir;
    }

    public File getLogDir()
    {
        return new File( serverDir, "logs" );
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public void setAdmin( boolean admin )
    {
        this.admin = admin;
    }

    public boolean isClustered()
    {
        return clustered;
    }

    protected void setClustered( boolean value )
    {
        clustered = value;
    }

    public int getAdminPort()
    {
        return adminPort;
    }

    public void setAdminPort( int adminPort )
    {
        this.adminPort = adminPort;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode( String mode )
    {
        this.mode = mode;
    }

    public boolean isProduction()
    {
        return ( !( getMode().equalsIgnoreCase( "dev" ) ) );
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain( String domain )
    {
        this.domain = domain;
    }

    public String getServerName()
    {
        return serverName;
    }

    public void setServerName( String serverName )
    {
        this.serverName = serverName;
    }

    public String getAdminHost()
    {
        return adminHost;
    }

    public void setAdminHost( String adminHost )
    {
        this.adminHost = adminHost;
    }

    @Override
    public String toString()
    {
        return "Env [admin=" + admin + ", adminHost=" + adminHost + ", adminPort=" + adminPort + ", mode=" + mode
            + ", serverDir=" + serverDir + ", serverName=" + serverName + "]";
    }
}