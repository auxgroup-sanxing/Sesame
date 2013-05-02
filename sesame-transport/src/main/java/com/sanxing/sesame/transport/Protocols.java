package com.sanxing.sesame.transport;

import com.sanxing.sesame.binding.transport.TransportFactory;
import com.sanxing.sesame.transport.impl.FileAcceptor;
import com.sanxing.sesame.transport.impl.FileConnector;
import com.sanxing.sesame.transport.impl.FtpAcceptor;
import com.sanxing.sesame.transport.impl.FtpConnector;
import com.sanxing.sesame.transport.impl.HTTPAcceptor;
import com.sanxing.sesame.transport.impl.HTTPConnector;
import com.sanxing.sesame.transport.impl.JMSTransport;
import com.sanxing.sesame.transport.impl.MQTransport;
import com.sanxing.sesame.transport.impl.MailAcceptor;
import com.sanxing.sesame.transport.impl.MailConnector;
import com.sanxing.sesame.transport.impl.SFTPAcceptor;
import com.sanxing.sesame.transport.impl.SFTPConnector;
import com.sanxing.sesame.transport.impl.SocketAcceptor;
import com.sanxing.sesame.transport.impl.SocketConnector;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class Protocols {
	private static AtomicBoolean registered = new AtomicBoolean(false);
	private static String[] standards;

	static {
		register();
	}

	public static void init() {
	}

	public static synchronized void register() {
		if (registered.get())
			return;

		TransportFactory.register("http", HTTPAcceptor.class);
		TransportFactory.register("http", HTTPConnector.class);

		TransportFactory.register("tcp", SocketAcceptor.class);
		TransportFactory.register("tcp", SocketConnector.class);

		TransportFactory.register("ftp", FtpAcceptor.class);
		TransportFactory.register("ftp", FtpConnector.class);
		TransportFactory.register("mail", MailAcceptor.class);
		TransportFactory.register("mail", MailConnector.class);

		TransportFactory.register("file", FileAcceptor.class);
		TransportFactory.register("file", FileConnector.class);

		TransportFactory.register("sftp", SFTPAcceptor.class);
		TransportFactory.register("sftp", SFTPConnector.class);

		TransportFactory.register("mq", MQTransport.class);
		TransportFactory.register("jms", JMSTransport.class);

		registered.set(true);
		standards = TransportFactory.getSchemes();
	}

	public static URL getSchema(String protocol, String style) {
		URL url = Protocols.class.getClassLoader().getResource(
				"schemes/" + protocol + "-" + style + ".xsd");
		return url;
	}

	public static URL getContextSchema(String protocol, String style) {
		URL url = Protocols.class.getClassLoader().getResource(
				"schemes/" + protocol + "-" + style + "-context" + ".xsd");
		return url;
	}

	public static String[] getStandards() {
		return standards;
	}

	public static String[] list() {
		return TransportFactory.getSchemes();
	}
}