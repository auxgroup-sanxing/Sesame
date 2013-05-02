package com.sanxing.sesame.serial;

public class NonceGenerator extends SerialGenerator {
	public long allocate() {
		return 1L;
	}

	public long getLimit() {
		return 9223372036854775807L;
	}
}