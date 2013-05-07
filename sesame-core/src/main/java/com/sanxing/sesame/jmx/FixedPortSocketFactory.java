package com.sanxing.sesame.jmx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedPortSocketFactory
    extends RMISocketFactory
{
    private final int fixedPort;

    Logger LOG = LoggerFactory.getLogger( FixedPortSocketFactory.class );

    public FixedPortSocketFactory( int port )
    {
        fixedPort = port;
    }

    @Override
    public Socket createSocket( String host, int port )
        throws IOException
    {
        LOG.info( "creating socket to host : " + host + "on port " + port );
        return new Socket( host, port );
    }

    @Override
    public ServerSocket createServerSocket( int port )
        throws IOException
    {
        port = ( port == 0 ) ? fixedPort : port;
        LOG.info( "creating ServerSocket on port " + port );
        return new ServerSocket( port );
    }
}