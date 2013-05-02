package com.sanxing.sesame.jmx;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedPortSocketFactory extends RMISocketFactory {
	private int fixedPort;
	Logger LOG = LoggerFactory.getLogger(FixedPortSocketFactory.class);

	public FixedPortSocketFactory(int port) {
		this.fixedPort = port;
	}

	public Socket createSocket(String host, int port) throws IOException {
		System.out.println("creating socket to host : " + host + "on port "
				+ port);
		return new Socket(host, port);
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		port = (port == 0) ? this.fixedPort : port;
		System.out.println("creating ServerSocket on port " + port);
		return new ServerSocket(port);
	}
}