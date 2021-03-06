/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.monitor;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.sanxing.sesame.bootstrap.log.LogProxy;

/**
 * Talks to the command monitor.
 */
public class CommandMonitorTalker
{

    /**
     * Logger. Uses log proxy to be able to redirect log output to System.out if SLF4J is not available.
     */
    private static LogProxy log = LogProxy.getLogger( CommandMonitorTalker.class );

    /**
     * 5 seconds in milliseconds. Used as default timeout.
     */
    private static final int FIVE_SECONDS = 5000;

    /**
     * Host to send commands to. Never null.
     */
    private final String host;

    /**
     * Port on host to send commands to. Bigger then 1.
     */
    private final int port;

    /**
     * Constructor.
     * 
     * @param host to send commands to. Cannot be null.
     * @param port on host to send commands to. Must be bigger then 1.
     */
    public CommandMonitorTalker( final String host, final int port )
    {
        if ( host == null )
        {
            throw new NullPointerException();
        }
        this.host = host;
        if ( port < 1 )
        {
            throw new IllegalArgumentException( "Invalid port" );
        }
        this.port = port;
    }

    /**
     * Sends a command to a {@link CommandMonitorThread} on configured host/port.
     * 
     * @param command to send. Cannot be null.
     * @throws Exception Re-thrown if sending command fails
     */
    public void send( final String command )
        throws Exception
    {
        send( command, FIVE_SECONDS );
    }

    /**
     * Sends a command to a {@link CommandMonitorThread} on configured host/port, timing out after the specified number
     * of milliseconds.
     * 
     * @param command to send. Cannot be null.
     * @param timeout number of milliseconds after which sending the command should timeout
     * @throws Exception Re-thrown if sending command fails
     */
    public void send( final String command, final int timeout )
        throws Exception
    {
        if ( command == null )
        {
            throw new NullPointerException();
        }

        log.debug( "Sending command: {}", command );

        Socket socket = new Socket();
        socket.setSoTimeout( timeout );
        socket.connect( new InetSocketAddress( host, port ) );
        try
        {
            OutputStream output = socket.getOutputStream();
            output.write( command.getBytes() );
            output.close();
        }
        finally
        {
            socket.close();
        }
    }

    /**
     * Returns the host to send commands to.
     * 
     * @return host to send commands to. Never null.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Returns the port on host to send commands to.
     * 
     * @return port on host to send commands to. Bigger then 1.
     */
    public String getPort()
    {
        return host;
    }

}
