/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.sanxing.sesame.bootstrap.log.LogProxy;
import com.sanxing.sesame.bootstrap.monitor.commands.PingCommand;

/**
 * Thread which listens for command messages and executes them if they match one of available commands.
 */
public class CommandMonitorThread
    extends Thread
{

    /**
     * Local host IP (127.0.0.1).
     */
    public static final String LOCALHOST = "127.0.0.1";

    /**
     * Logger. Uses log proxy to be able to redirect log output to System.out if SLF4J is not available.
     */
    private static final LogProxy log = LogProxy.getLogger( CommandMonitorThread.class );

    /**
     * Listening socket. Never null.
     */
    private final ServerSocket socket;

    /**
     * List of available commands.
     */
    private final Map<String, Command> commands = new HashMap<String, Command>();

    /**
     * Constructor.
     * 
     * @param port port on which to listen for commands. If zero, an random port will be chosen.
     * @param commands available commands. Can be empty.
     * @throws IOException Re-thrown while opening listening socket
     */
    public CommandMonitorThread( final int port, final Command... commands )
        throws IOException
    {
        setDaemon( true );

        if ( commands != null )
        {
            for ( final Command command : commands )
            {
                this.commands.put( command.getId(), command );
            }
        }

        setDaemon( true );
        setName( "Bootstrap Command Monitor" );

        // Only listen on local interface
        socket = new ServerSocket( port, 1, InetAddress.getByName( LOCALHOST ) );
    }

    /**
     * Listens for commands on configured port on local interface.
     */
    @Override
    public void run()
    {
        log.debug( "Listening for commands: {}", socket );

        boolean running = true;
        while ( running )
        {
            try
            {
                Socket client = socket.accept();
                log.debug( "Accepted client: {}", client );

                BufferedReader reader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
                String commandId = reader.readLine();
                log.debug( "Read command: {}", commandId );
                client.close();

                if ( commandId == null )
                {
                    commandId = PingCommand.NAME;
                }
                final Command command = commands.get( commandId );
                if ( command == null )
                {
                    log.error( "Unknown command: {}", commandId );
                }
                else
                {
                    running = !command.execute();
                }
            }
            catch ( Exception e )
            {
                log.error( "Failed", e );
            }
        }

        try
        {
            socket.close();
        }
        catch ( IOException e )
        {
            // ignore
        }

        log.debug( "Stopped" );
    }

    /**
     * Returns the port, the monitor, it listens to. Is the provided one or the random generated one if port used in
     * constructor was null.
     * 
     * @return monitor port. Bigger then 0.
     */
    public int getPort()
    {
        return socket.getLocalPort();
    }

    /**
     * A command to be executed in case that received command matches.
     * 
     * @since 2.2
     */
    public static interface Command
    {

        /**
         * ID of command (when it should be executed).
         * 
         * @return command id. Never null.
         */
        String getId();

        /**
         * Executes the command.
         * 
         * @return true, if command monitor thread should stop running
         */
        boolean execute();

    }

}
