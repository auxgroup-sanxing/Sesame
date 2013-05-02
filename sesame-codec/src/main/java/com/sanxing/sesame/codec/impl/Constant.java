package com.sanxing.sesame.codec.impl;

public abstract interface Constant {
	public static final int int_ = 4;
	public static final int unsignedInt_ = 4;
	public static final int short_ = 2;
	public static final int unsignedshort_ = 2;
	public static final int long_ = 8;
	public static final int unsignedlong_ = 8;
	public static final int float_ = 4;
	public static final int double_ = 8;
	public static final String KIND_F = "F";
	public static final String KIND_V = "V";
	public static final String KIND_S = "S";
	public static final String ALIGN_L = "L";
	public static final String ALIGN_R = "R";
	public static final String ENDIAN_BIG = "big";
	public static final String ENDIAN_LITTLE = "little";
	public static final int COMPRESS_ON = 1;
	public static final int COMPRESS_OFF = 0;
}