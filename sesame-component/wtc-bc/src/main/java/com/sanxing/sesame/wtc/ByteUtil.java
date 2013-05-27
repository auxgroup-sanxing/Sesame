package com.sanxing.sesame.wtc;

import com.sanxing.sesame.wtc.output.SOPOutputter;
import java.io.PrintStream;

public class ByteUtil
{
  public static byte[] cancat(byte[] a, byte[] b)
  {
    int alen = a.length;
    int blen = b.length;
    byte[] result = new byte[alen + blen];
    System.arraycopy(a, 0, result, 0, alen);
    System.arraycopy(b, 0, result, alen, blen);
    return result;
  }

  public static int getInt(byte[] bb, int index) {
    return (bb[(index + 0)] & 0xFF) << 24 | 
      (bb[(index + 1)] & 0xFF) << 16 | 
      (bb[(index + 2)] & 0xFF) << 8 | (bb[(index + 3)] & 0xFF) << 0;
  }

  public static short getShort(byte[] b, int index) {
    return (short)(b[index] << 8 | b[(index + 1)] & 0xFF);
  }

  public static byte[] intToByte(int number) {
    int temp = number;
    byte[] b = new byte[4];
    for (int i = 0; i < b.length; i++) {
      b[i] = new Integer(temp & 0xFF).byteValue();
      temp >>= 8;
    }
    return b;
  }

  public static byte[] shortToByte(short number) {
    int temp = number;
    byte[] b = new byte[2];
    for (int i = b.length - 1; i >= 0; i--) {
      b[i] = new Integer(temp & 0xFF).byteValue();
      temp >>= 8;
    }
    return b;
  }

  public static String bytesToHexString(byte[] src)
  {
    StringBuilder stringBuilder = new StringBuilder("");
    if ((src == null) || (src.length <= 0)) {
      return null;
    }
    for (int i = 0; i < src.length; i++) {
      int v = src[i] & 0xFF;
      String hv = Integer.toHexString(v);
      if (hv.length() < 2) {
        stringBuilder.append(0);
      }
      stringBuilder.append(hv);
    }
    return stringBuilder.toString();
  }

  public static byte[] hexStringToBytes(String hexString)
  {
    if ((hexString == null) || (hexString.equals(""))) {
      return null;
    }
    hexString = hexString.toUpperCase();
    int length = hexString.length() / 2;
    char[] hexChars = hexString.toCharArray();
    byte[] d = new byte[length];
    for (int i = 0; i < length; i++) {
      int pos = i * 2;
      d[i] = ((byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)])));
    }
    return d;
  }

  public static byte charToByte(char c) {
    return (byte)"0123456789ABCDEF".indexOf(c);
  }

  public static void main(String[] args) {
    byte[] bytes = hexStringToBytes("39");
    System.out.println(SOPOutputter.format(bytes));
  }
}



