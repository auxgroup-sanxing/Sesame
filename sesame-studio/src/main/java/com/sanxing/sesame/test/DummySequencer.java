package com.sanxing.statenet.test;

public class DummySequencer {
	private static long serial = -1L;

	public static long getSerial() {
		return (serial--);
	}
}