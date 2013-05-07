package com.sanxing.sesame.uuid;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(IdGenerator.class);
	private static final String UNIQUE_STUB;
	private static int instanceCount;
	private static String hostName;
	private String seed;
	private AtomicLong sequence;

	public IdGenerator() {
		this("ID:");
	}

	public IdGenerator(String prefix) {
		this.sequence = new AtomicLong(0L);

		synchronized (UNIQUE_STUB) {
			this.seed = prefix + UNIQUE_STUB + (instanceCount++) + ":";
		}
	}

	public static String getHostName() {
		return hostName;
	}

	public String generateId() {
		return this.seed + this.sequence.getAndDecrement();
	}

	public String generateSanitizedId() {
		String result = generateId();
		result = result.replace(':', '-');
		result = result.replace('_', '-');
		result = result.replace('.', '-');
		return result;
	}

	static {
		String stub = "";
		boolean canAccessSystemProps = true;
		try {
			SecurityManager sm = System.getSecurityManager();
			if (sm != null)
				sm.checkPropertiesAccess();
		} catch (SecurityException se) {
			canAccessSystemProps = false;
		}

		if (canAccessSystemProps) {
			try {
				hostName = InetAddress.getLocalHost().getHostAddress();
				ServerSocket ss = new ServerSocket(0);
				stub = hostName
						+ "-"
						+ Long.toHexString(ss.getLocalPort()
								^ System.currentTimeMillis()) + "-";
				Thread.sleep(100L);
				ss.close();
			} catch (Exception ioe) {
				LOG.warn("Could not generate unique stub", ioe);
			}
		} else {
			hostName = "localhost";
			stub = hostName + Long.toHexString(System.currentTimeMillis())
					+ "-";
		}
		UNIQUE_STUB = stub;
	}
}