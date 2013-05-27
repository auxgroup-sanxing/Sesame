package com.sanxing.sesame.wtc.sop;

import com.sanxing.sesame.wtc.ByteUtil;
import com.sanxing.sesame.wtc.Mac;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SOPUtil
{
  public static String charset = "GBK";

  public static boolean verifyMAC(byte[] sopBuf) {
    byte[] mac = getMAC(sopBuf);
    byte[] enc = getEncrypt(sopBuf);
    if (enc[0] == 48) {
      return true;
    }

    int pos = 18;
    int len = sopBuf.length - pos;
    byte[] content = new byte[len];
    System.arraycopy(sopBuf, pos, content, 0, len);

    boolean result = new Mac().verify(content, mac);
    return result;
  }

  public static byte[] getEncrypt(byte[] sopBuf) {
    byte[] enc = new byte[1];
    System.arraycopy(sopBuf, 44, enc, 0, 1);
    return enc;
  }

  public static byte[] getMAC(byte[] sopBuf) {
    byte[] mac = new byte[16];
    System.arraycopy(sopBuf, 2, mac, 0, 
      16);
    return mac;
  }

  public static byte[] gettranCode(byte[] sopBuf) {
    byte[] tranCodeBytes = new byte[4];
    System.arraycopy(sopBuf, 21, tranCodeBytes, 
      0, 4);
    return tranCodeBytes;
  }

  public static void putField(ByteBuffer buf, String field)
  {
    try
    {
      byte[] bytes = field.getBytes(charset);
      putField(buf, bytes);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public static void putFixField(ByteBuffer buf, String field, int length)
  {
    try
    {
      byte[] result = new byte[length];
      Arrays.fill(result, (byte)0);

      byte[] bytes = field.getBytes(charset);
      length = length < bytes.length ? length : bytes.length;
      System.arraycopy(bytes, 0, result, 0, length);
      for (int index = 0; index < result.length; index++)
        buf.put(result[index]);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public static byte[] getBytes(byte[] bytes, int start, int length)
  {
    length = length < bytes.length - start ? length : 
      bytes.length - start;
    byte[] result = new byte[length];
    System.arraycopy(bytes, start, result, 0, length);
    return result;
  }

  public static String getField(byte[] bytes, int start, int length)
  {
    byte[] result = getBytes(bytes, start, length);
    try {
      return new String(result, charset);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }return "";
  }

  public static int getInt(byte[] bytes, int start)
  {
    int length = 4;
    byte[] result = getBytes(bytes, start, length);
    return ByteUtil.getInt(result, 0);
  }

  public static short getShort(byte[] bytes, int start)
  {
    int length = 2;
    byte[] result = getBytes(bytes, start, length);
    return ByteUtil.getShort(result, 0);
  }

  private static void putField(ByteBuffer buf, byte[] bytes) {
    int length = bytes.length;
    if (length < 250) {
      putSingleField(buf, bytes);
    } else {
      int len = 250;
      int rlen = length - len;
      byte[] front = new byte[len];
      byte[] end = new byte[rlen];
      System.arraycopy(bytes, 0, front, 0, len);
      System.arraycopy(bytes, len, end, 0, rlen);
      putSingleField(buf, front);

      putField(buf, end);
    }
  }

  private static void putSingleField(ByteBuffer buf, byte[] bytes) {
    int length = bytes.length;
    int putLength = length;
    if (length == 250) {
      putLength = 255;
    }
    buf.put((byte)putLength);
    for (int index = 0; index < length; index++)
      buf.put(bytes[index]);
  }

  public static String format(String name, String value)
  {
    return String.format("%-20s\t[%s]\n", new Object[] { name, value });
  }

  public static String format(String name, int value) {
    return String.format("%-20s\t[%d]\n", new Object[] { name, Integer.valueOf(value) });
  }

  public static String format(String name, short value) {
    return String.format("%-20s\t[%d]\n", new Object[] { name, Short.valueOf(value) });
  }

  public static void main(String[] argc) {
    byte[] bytes = { 77, 65, 67, 32, -48, -93, 
      -47, -23, -54, -89, 
      -80, -36 };
    try
    {
      System.out.println(new String(bytes, "GBK"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}



