package com.sanxing.sesame.util;

public class UnicodeFormatter {
	public static char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String byteToHex(byte b) {
		char[] array = { HEX_DIGIT[(b >> 4 & 0xF)], HEX_DIGIT[(b & 0xF)] };

		return new String(array);
	}

	public static String charToHex(char c) {
		byte hi = (byte) (c >>> '\b');
		byte lo = (byte) (c & 0xFF);

		return byteToHex(hi) + byteToHex(lo);
	}

	public static String toString(char[] array) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < array.length; ++i) {
			sb.append("\\u");
			sb.append(charToHex(array[i]));
		}

		return sb.toString();
	}

	public static String toString(String s) {
		if (s == null) {
			return null;
		}

		return toString(s.toCharArray());
	}
}