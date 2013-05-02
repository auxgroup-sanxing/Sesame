package com.sanxing.sesame.util;

import java.io.PrintStream;
import java.net.InetAddress;

public class NetworkUtil {
	public static String getMyIP() {
		try {
			InetAddress address = InetAddress.getLocalHost();
			return address.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "127.0.0.1";
	}

	public static void main(String[] args) {
		System.out.println(getMyIP());
	}
}