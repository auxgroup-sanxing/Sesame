package com.sanxing.sesame.wtc;

public abstract interface SOPConstants
{
  public static final int SOP_UNIT_MAX = 250;
  public static final int SOP_DECODE_UNIT_MAX = 255;
  public static final int SOP_SYS_HEAD = 107;
  public static final int SOP_REQ_PUB_HEAD = 21;
  public static final int SOP_REQ_TRAN_HEAD = 64;
  public static final int SOP_RES_PUB_HEAD = 41;
  public static final int SOP_PACKAGE = 2;
  public static final String CHANNEL_FROM = "channel_from";
  public static final String CHANNEL_TO = "channel_to";
  public static final int CHANNEL_FROM_OFFSET = 38;
  public static final int CHANNEL_TO_OFFSET = 41;
  public static final String PIN_FLAG = "pin_flag";
  public static final String PIN_SEED = "pin_seed";
  public static final int PIN_FLAG_OFFSET = 45;
  public static final int PIN_SEED_OFFSET = 22;
  public static final int PIN_FLAG_SIZE = 1;
  public static final int PIN_SEED_SIZE = 16;
  public static final int PASS_OFFSET_SIZE = 20;
  public static final int ENCRYPT_OFFSET = 44;
  public static final int MAC_OFFSET = 2;
  public static final int MAC_SIZE = 16;
  public static final int CHANNEL_SIZE = 3;
  public static final int TRANCODE_OFFSET = 21;
  public static final int ERRCODE_OFFSET = 34;
  public static final int ERRCODE_SIZE = 7;
  public static final String SUCCESS = "AAAAAAA";
}