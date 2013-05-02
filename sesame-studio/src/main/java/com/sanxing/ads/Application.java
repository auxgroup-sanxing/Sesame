package com.sanxing.ads;

import com.sanxing.ads.team.SCM;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

public class Application implements ServletContextListener {
	public static final String ENCRYPT_ALGORITHM = "RSA";
	public static final int KEY_SIZE = 512;
	private static final Logger LOG = Logger.getLogger(Application.class);

	private static ServletContext servletContext = null;
	private static File projectFolder;
	private static File wareFolder;
	private static KeyPair keyPair;

	private static ServletContext getServletContext() {
		if (servletContext == null) {
			throw new RuntimeException("Application does not Initialized");
		}
		return servletContext;
	}

	public static String getRealPath(String path) {
		return getServletContext().getRealPath(path);
	}

	public static File getWarehouseRoot() {
		return wareFolder;
	}

	public static File getWarehouseFile(String path) {
		return new File(getWarehouseRoot(), path);
	}

	public static File getWorkspaceRoot() {
		return projectFolder;
	}

	public static File getWorkspaceFile(String path) {
		return new File(getWorkspaceRoot(), path);
	}

	public static KeyPair getKeyPair() {
		return keyPair;
	}

	public void contextDestroyed(ServletContextEvent event) {
		servletContext = null;
		SQLDataSource.closeDataSource();
		SCM.cleanUp();
	}

	public void contextInitialized(ServletContextEvent event) {
		servletContext = event.getServletContext();

		String home = System.getProperty("STATENET_HOME");
		projectFolder = new File(home, "projects");
		if (!(projectFolder.exists())) {
			projectFolder.mkdir();
		}

		wareFolder = new File(home, "warehouse");
		if (!(wareFolder.exists())) {
			wareFolder.mkdir();
		}

		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(512);
			keyPair = generator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}