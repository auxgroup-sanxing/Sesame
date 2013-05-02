package com.sanxing.sesame.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

public class PasswordAuthenticator implements JMXAuthenticator {
	private static final String LEFT_DELIMITER = "OBF(";
	private static final String RIGHT_DELIMITER = "):";
	private Map passwords;

	public PasswordAuthenticator(File passwordFile) throws IOException {
		this(new FileInputStream(passwordFile));
	}

	public PasswordAuthenticator(InputStream is) throws IOException {
		this.passwords = readPasswords(is);
	}

	public static void main(String[] args) throws Exception {
		if ((args.length == 1) && (!("-help".equals(args[0])))) {
			printPassword("MD5", args[0]);
			return;
		}
		if ((args.length == 3) && ("-alg".equals(args[0]))) {
			printPassword(args[1], args[2]);
			return;
		}
		printUsage();
	}

	private static void printPassword(String algorithm, String input) {
		String password = obfuscatePassword(input, algorithm);
		System.out.println(password);
	}

	private static void printUsage() {
		System.out.println();
		System.out
				.println("Usage: java -cp <lib>/mx4j-tools.jar mx4j.tools.remote.PasswordAuthenticator <options> <password>");
		System.out.println("Where <options> is one of the following:");
		System.out.println("   -help                     Prints this message");
		System.out
				.println("   -alg <digest algorithm>   Specifies the digest algorithm (default is MD5)");
		System.out.println();
	}

	public static String obfuscatePassword(String password) {
		return obfuscatePassword(password, "MD5");
	}

	public static String obfuscatePassword(String password, String algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			byte[] digestedBytes = digest.digest(password.getBytes());
			byte[] obfuscatedBytes = Base64Codec.encodeBase64(digestedBytes);
			return "OBF(" + algorithm + "):" + new String(obfuscatedBytes);
		} catch (NoSuchAlgorithmException x) {
			throw new SecurityException("Could not find digest algorithm "
					+ algorithm);
		}
	}

	private Map readPasswords(InputStream is) throws IOException {
		Properties properties = new Properties();
		try {
			properties.load(is);
		} finally {
			is.close();
		}
		return new HashMap(properties);
	}

	public Subject authenticate(Object credentials) throws SecurityException {
		if (!(credentials instanceof String[])) {
			throw new SecurityException("Bad credentials");
		}
		String[] creds = (String[]) credentials;
		if (creds.length != 2) {
			throw new SecurityException("Bad credentials");
		}

		String user = creds[0];
		String password = creds[1];

		if (password == null) {
			throw new SecurityException("Bad password");
		}

		if (!(this.passwords.containsKey(user))) {
			throw new SecurityException("Unknown user " + user);
		}

		String storedPassword = (String) this.passwords.get(user);
		if (!(isPasswordCorrect(password, storedPassword))) {
			throw new SecurityException("Bad password");
		}

		Set principals = new HashSet();
		principals.add(new JMXPrincipal(user));
		return new Subject(true, principals, Collections.EMPTY_SET,
				Collections.EMPTY_SET);
	}

	private boolean isPasswordCorrect(String password, String storedPassword) {
		if (password.startsWith("OBF(")) {
			if (storedPassword.startsWith("OBF(")) {
				return password.equals(storedPassword);
			}
			String algorithm = getAlgorithm(password);
			String obfuscated = obfuscatePassword(storedPassword, algorithm);
			return password.equals(obfuscated);
		}

		if (storedPassword.startsWith("OBF(")) {
			String algorithm = getAlgorithm(storedPassword);
			String obfuscated = obfuscatePassword(password, algorithm);
			return obfuscated.equals(storedPassword);
		}
		return password.equals(storedPassword);
	}

	private String getAlgorithm(String obfuscatedPassword) {
		try {
			return obfuscatedPassword.substring("OBF(".length(),
					obfuscatedPassword.indexOf("):"));
		} catch (IndexOutOfBoundsException x) {
			throw new SecurityException("Bad password");
		}
	}
}