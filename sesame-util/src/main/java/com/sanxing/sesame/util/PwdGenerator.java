package com.sanxing.sesame.util;

public class PwdGenerator {
	public static String KEY1 = "0123456789";

	public static String KEY2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String KEY3 = "abcdefghijklmnopqrstuvwxyz";

	public static String getPinNumber() {
		return _getPassword(KEY1, 4);
	}

	public static String getPassword() {
		return getPassword(8);
	}

	public static String getPassword(int length) {
		return _getPassword(KEY1 + KEY2 + KEY3, length);
	}

	public static String getPassword(String key, int length) {
		return _getPassword(key, length);
	}

	private static String _getPassword(String key, int length) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; ++i) {
			sb.append(key.charAt((int) (Math.random() * key.length())));
		}

		return sb.toString();
	}
}