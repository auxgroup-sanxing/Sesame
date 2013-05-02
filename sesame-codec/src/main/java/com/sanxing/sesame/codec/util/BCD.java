package com.sanxing.sesame.codec.util;

import com.sanxing.sesame.binding.codec.FormatException;
import java.io.ByteArrayOutputStream;

public class BCD {
	public static String bcd2str(byte[] bt, int len, String compressAlign)
			throws FormatException {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < bt.length; ++i) {
			if (len % 2 != 0) {
				if ("R".equals(compressAlign)) {
					if (i == 0) {
						byteL2Char(bt[i], sb);
					} else {
						byteH2Char(bt[i], sb);
						byteL2Char(bt[i], sb);
					}
				} else if (i != bt.length - 1) {
					byteH2Char(bt[i], sb);
					byteL2Char(bt[i], sb);
				} else {
					byteH2Char(bt[i], sb);
				}
			} else {
				byteH2Char(bt[i], sb);
				byteL2Char(bt[i], sb);
			}
		}
		return sb.toString();
	}

	private static void byteH2Char(byte bt, StringBuffer sb)
			throws FormatException {
		if (((bt & 0xF0) >>> 4 >= 10) && ((bt & 0xF0) >>> 4 <= 16))
			sb.append((char) (((bt & 0xF0) >>> 4) - 10 + 97));
		else if (((bt & 0xF0) >>> 4 >= 0) && ((bt & 0xF0) >>> 4 <= 9))
			sb.append((char) (((bt & 0xF0) >>> 4) + 48));
		else
			throw new FormatException("decode error char!");
	}

	private static void byteL2Char(byte bt, StringBuffer sb)
			throws FormatException {
		if (((bt & 0xF) >= 10) && ((bt & 0xF) <= 16))
			sb.append((char) ((bt & 0xF) - 10 + 97));
		else if (((bt & 0xF) >= 0) && ((bt & 0xF) <= 9))
			sb.append((char) ((bt & 0xF) + 48));
		else
			throw new FormatException("decode error char!");
	}

	public static byte[] str2bcd(String string, String align, int blank)
			throws FormatException {
		int len = string.length() % 2;
		if (len != 0) {
			char compressBlank = (char) blank;
			if ("R".equals(align))
				string = compressBlank + string;
			else
				string = string + compressBlank;
		}
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		char[] ch = string.toCharArray();
		for (int i = 0; i < ch.length; i += 2) {
			int hight = 0;
			int low = 0;
			hight = char2Int(ch[i]);
			low = char2Int(ch[(i + 1)]);
			bs.write(hight << 4 | low);
		}

		return bs.toByteArray();
	}

	private static int char2Int(char ch) throws FormatException {
		int intValue = 0;
		if ((ch >= '0') && (ch <= '9')) {
			intValue = ch - '0';
		} else if ((ch >= 'a') && (ch <= 'f')) {
			intValue = ch - 'a' + 10;
		} else if ((ch >= 'A') && (ch <= 'F'))
			intValue = ch - 'A' + 10;
		else {
			throw new FormatException("error char [" + ch + "]");
		}
		return intValue;
	}

	public static byte[] getBinaryBuf(int len, int compress) {
		byte[] temp;
		if (1 == compress) {
			if (len % 2 == 0)
				temp = new byte[len / 2];
			else
				temp = new byte[len / 2 + 1];
		} else {
			temp = new byte[len];
		}
		return temp;
	}
}