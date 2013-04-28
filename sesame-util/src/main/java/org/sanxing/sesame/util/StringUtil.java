package org.sanxing.sesame.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class StringUtil {
	public static String add(String s, String add) {
		return add(s, add, ",");
	}

	public static String add(String s, String add, String delimiter) {
		return add(s, add, delimiter, false);
	}

	public static String add(String s, String add, String delimiter,
			boolean allowDuplicates) {
		if ((add == null) || (delimiter == null)) {
			return null;
		}

		if (s == null) {
			s = "";
		}

		if ((allowDuplicates) || (!(contains(s, add, delimiter)))) {
			if ((Validator.isNull(s)) || (s.endsWith(delimiter))) {
				s = s + add + delimiter;
			} else {
				s = s + delimiter + add + delimiter;
			}
		}

		return s;
	}

	public static boolean contains(String s, String text) {
		return contains(s, text, ",");
	}

	public static boolean contains(String s, String text, String delimiter) {
		if ((s == null) || (text == null) || (delimiter == null)) {
			return false;
		}

		if (!(s.endsWith(delimiter))) {
			s = s + delimiter;
		}

		int pos = s.indexOf(delimiter + text + delimiter);

		if (pos == -1) {
			return (s.startsWith(text + delimiter));
		}

		return true;
	}

	public static int count(String s, String text) {
		if ((s == null) || (text == null)) {
			return 0;
		}

		int count = 0;

		int pos = s.indexOf(text);

		while (pos != -1) {
			pos = s.indexOf(text, pos + text.length());
			++count;
		}

		return count;
	}

	public static boolean endsWith(String s, char end) {
		return startsWith(s, new Character(end).toString());
	}

	public static boolean endsWith(String s, String end) {
		if ((s == null) || (end == null)) {
			return false;
		}

		if (end.length() > s.length()) {
			return false;
		}

		String temp = s.substring(s.length() - end.length(), s.length());

		return temp.equalsIgnoreCase(end);
	}

	public static String extractChars(String s) {
		if (s == null) {
			return "";
		}

		char[] c = s.toCharArray();

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < c.length; ++i) {
			if (Validator.isChar(c[i])) {
				sb.append(c[i]);
			}
		}

		return sb.toString();
	}

	public static String extractDigits(String s) {
		if (s == null) {
			return "";
		}

		char[] c = s.toCharArray();

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < c.length; ++i) {
			if (Validator.isDigit(c[i])) {
				sb.append(c[i]);
			}
		}

		return sb.toString();
	}

	public static String extractFirst(String s, String delimiter) {
		if (s == null) {
			return null;
		}

		String[] array = split(s, delimiter);

		if (array.length > 0) {
			return array[0];
		}

		return null;
	}

	public static String extractLast(String s, String delimiter) {
		if (s == null) {
			return null;
		}

		String[] array = split(s, delimiter);

		if (array.length > 0) {
			return array[(array.length - 1)];
		}

		return null;
	}

	public static String lowerCase(String s) {
		if (s == null) {
			return null;
		}

		return s.toLowerCase();
	}

	public static String merge(List list) {
		return merge(list, ",");
	}

	public static String merge(List list, String delimiter) {
		return merge((String[]) list.toArray(new String[0]), delimiter);
	}

	public static String merge(String[] array) {
		return merge(array, ",");
	}

	public static String merge(String[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < array.length; ++i) {
			sb.append(array[i].trim());

			if (i + 1 != array.length) {
				sb.append(delimiter);
			}
		}

		return sb.toString();
	}

	public static String randomize(String s) {
		return Randomizer.getInstance().randomize(s);
	}

	public static String read(ClassLoader classLoader, String name)
			throws IOException {
		return read(classLoader.getResourceAsStream(name));
	}

	public static String read(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append('\n');
		}

		br.close();

		return sb.toString().trim();
	}

	public static String remove(String s, String remove) {
		return remove(s, remove, ",");
	}

	public static String remove(String s, String remove, String delimiter) {
		if ((s == null) || (remove == null) || (delimiter == null)) {
			return null;
		}

		if ((Validator.isNotNull(s)) && (!(s.endsWith(delimiter)))) {
			s = s + delimiter;
		}

		while (contains(s, remove, delimiter)) {
			int pos = s.indexOf(delimiter + remove + delimiter);

			if (pos == -1) {
				if (s.startsWith(remove + delimiter)) {
					int x = remove.length() + delimiter.length();
					int y = s.length();

					s = s.substring(x, y);
				}
			} else {
				int x = pos + remove.length() + delimiter.length();
				int y = s.length();

				s = s.substring(0, pos) + s.substring(x, y);
			}
		}

		return s;
	}

	public static String replace(String s, char oldSub, char newSub) {
		return replace(s, oldSub, Character.toString(newSub));
	}

	public static String replace(String s, char oldSub, String newSub) {
		if ((s == null) || (newSub == null)) {
			return null;
		}

		char[] c = s.toCharArray();

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < c.length; ++i) {
			if (c[i] == oldSub) {
				sb.append(newSub);
			} else {
				sb.append(c[i]);
			}
		}

		return sb.toString();
	}

	public static String replace(String s, String oldSub, String newSub) {
		if ((s == null) || (oldSub == null) || (newSub == null)) {
			return null;
		}

		int y = s.indexOf(oldSub);

		if (y >= 0) {
			StringBuffer sb = new StringBuffer();

			int length = oldSub.length();
			int x = 0;

			while (x <= y) {
				sb.append(s.substring(x, y));
				sb.append(newSub);
				x = y + length;
				y = s.indexOf(oldSub, x);
			}

			sb.append(s.substring(x));

			return sb.toString();
		}

		return s;
	}

	public static String replace(String s, String[] oldSubs, String[] newSubs) {
		if ((s == null) || (oldSubs == null) || (newSubs == null)) {
			return null;
		}

		if (oldSubs.length != newSubs.length) {
			return s;
		}

		for (int i = 0; i < oldSubs.length; ++i) {
			s = replace(s, oldSubs[i], newSubs[i]);
		}

		return s;
	}

	public static String reverse(String s) {
		if (s == null) {
			return null;
		}

		char[] c = s.toCharArray();
		char[] reverse = new char[c.length];

		for (int i = 0; i < c.length; ++i) {
			reverse[i] = c[(c.length - i - 1)];
		}

		return new String(reverse);
	}

	public static String shorten(String s) {
		return shorten(s, 20);
	}

	public static String shorten(String s, int length) {
		return shorten(s, length, "...");
	}

	public static String shorten(String s, String suffix) {
		return shorten(s, 20, suffix);
	}

	public static String shorten(String s, int length, String suffix) {
		if ((s == null) || (suffix == null)) {
			return null;
		}

		if (s.length() > length) {
			for (int j = length; j >= 0; --j) {
				if (Character.isWhitespace(s.charAt(j))) {
					length = j;

					break;
				}
			}

			s = s.substring(0, length) + suffix;
		}

		return s;
	}

	public static String[] split(String s) {
		return split(s, ",");
	}

	public static String[] split(String s, String delimiter) {
		if ((s == null) || (delimiter == null)) {
			return new String[0];
		}

		s = s.trim();

		if (!(s.endsWith(delimiter))) {
			s = s + delimiter;
		}

		if (s.equals(delimiter)) {
			return new String[0];
		}

		List nodeValues = new ArrayList();

		if ((delimiter.equals("\n")) || (delimiter.equals("\r"))) {
			try {
				BufferedReader br = new BufferedReader(new StringReader(s));
				String line;
				while ((line = br.readLine()) != null) {
					nodeValues.add(line);
				}

				br.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else {
			int offset = 0;
			int pos = s.indexOf(delimiter, offset);

			while (pos != -1) {
				nodeValues.add(s.substring(offset, pos));

				offset = pos + delimiter.length();
				pos = s.indexOf(delimiter, offset);
			}
		}

		return ((String[]) nodeValues.toArray(new String[0]));
	}

	public static boolean[] split(String s, String delimiter, boolean x) {
		String[] array = split(s, delimiter);
		boolean[] newArray = new boolean[array.length];

		for (int i = 0; i < array.length; ++i) {
			boolean value = x;
			try {
				value = Boolean.valueOf(array[i]).booleanValue();
			} catch (Exception localException) {
			}
			newArray[i] = value;
		}

		return newArray;
	}

	public static double[] split(String s, String delimiter, double x) {
		String[] array = split(s, delimiter);
		double[] newArray = new double[array.length];

		for (int i = 0; i < array.length; ++i) {
			double value = x;
			try {
				value = Double.parseDouble(array[i]);
			} catch (Exception localException) {
			}
			newArray[i] = value;
		}

		return newArray;
	}

	public static float[] split(String s, String delimiter, float x) {
		String[] array = split(s, delimiter);
		float[] newArray = new float[array.length];

		for (int i = 0; i < array.length; ++i) {
			float value = x;
			try {
				value = Float.parseFloat(array[i]);
			} catch (Exception localException) {
			}
			newArray[i] = value;
		}

		return newArray;
	}

	public static int[] split(String s, String delimiter, int x) {
		String[] array = split(s, delimiter);
		int[] newArray = new int[array.length];

		for (int i = 0; i < array.length; ++i) {
			int value = x;
			try {
				value = Integer.parseInt(array[i]);
			} catch (Exception localException) {
			}
			newArray[i] = value;
		}

		return newArray;
	}

	public static long[] split(String s, String delimiter, long x) {
		String[] array = split(s, delimiter);
		long[] newArray = new long[array.length];

		for (int i = 0; i < array.length; ++i) {
			long value = x;
			try {
				value = Long.parseLong(array[i]);
			} catch (Exception localException) {
			}
			newArray[i] = value;
		}

		return newArray;
	}

	public static short[] split(String s, String delimiter, short x) {
		String[] array = split(s, delimiter);
		short[] newArray = new short[array.length];

		for (int i = 0; i < array.length; ++i) {
			short value = x;
			try {
				value = Short.parseShort(array[i]);
			} catch (Exception localException) {
			}
			newArray[i] = value;
		}

		return newArray;
	}

	public static boolean startsWith(String s, char begin) {
		return startsWith(s, new Character(begin).toString());
	}

	public static boolean startsWith(String s, String start) {
		if ((s == null) || (start == null)) {
			return false;
		}

		if (start.length() > s.length()) {
			return false;
		}

		String temp = s.substring(0, start.length());

		return (temp.equalsIgnoreCase(start));
	}

	public static String trimLeading(String s) {
		for (int i = 0; i < s.length(); ++i) {
			if (!(Character.isWhitespace(s.charAt(i)))) {
				return s.substring(i, s.length());
			}
		}

		return "";
	}

	public static String trimTrailing(String s) {
		for (int i = s.length() - 1; i >= 0; --i) {
			if (!(Character.isWhitespace(s.charAt(i)))) {
				return s.substring(0, i + 1);
			}
		}

		return "";
	}

	public static String upperCase(String s) {
		if (s == null) {
			return null;
		}

		return s.toUpperCase();
	}

	public static String wrap(String text) {
		return wrap(text, 80, "\n");
	}

	public static String wrap(String text, int width, String lineSeparator) {
		if (text == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new StringReader(text));

			String s = "";

			while ((s = br.readLine()) != null)
				if (s.length() == 0) {
					sb.append(lineSeparator);
				} else {
					String[] tokens = s.split(" ");
					boolean firstWord = true;
					int curLineLength = 0;

					for (int i = 0; i < tokens.length; ++i) {
						if (!(firstWord)) {
							sb.append(" ");
							++curLineLength;
						}

						if (firstWord) {
							sb.append(lineSeparator);
						}

						sb.append(tokens[i]);

						curLineLength += tokens[i].length();

						if (curLineLength >= width) {
							firstWord = true;
							curLineLength = 0;
						} else {
							firstWord = false;
						}
					}
				}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return sb.toString();
	}
}