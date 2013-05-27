package com.sanxing.sesame.wtc.output;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SOPOutputter
{
  public static String format(byte[] src)
  {
    return format(src, new SOPFormat());
  }

  public static String format(byte[] src, SOPFormat format)
  {
    StringBuilder stringBuilder = new StringBuilder("");
    String prompt = null;

    if (format.getHeader() != null) {
      stringBuilder.append(format.getHeader());
    }
    if ((src == null) || (src.length <= 0)) {
      return null;
    }

    int position = 0;
    for (int i = 0; i < src.length; i++)
    {
      if (i % format.getColumns() == 0) {
        stringBuilder.append("\n");
        prompt = String.format(format.getLinePrompt(), new Object[] { 
          Integer.toHexString(i) });
        stringBuilder.append(prompt);
        position = 0;
      }

      int v = src[i] & 0xFF;
      String hex = Integer.toHexString(v);
      if (hex.length() < 2) {
        stringBuilder.append(0);
      }
      stringBuilder.append(hex.toUpperCase());
      stringBuilder.append(format.getSeprator());

      position++;

      if ((position % format.getColumns() == 0) || (i == src.length - 1))
      {
        if (position % format.getColumns() != 0) {
          int left = format.getColumns() - position;
          while (left > 0) {
            stringBuilder.append("  ");
            stringBuilder.append(format.getSeprator());
            left--;
          }
        }

        byte[] dest = new byte[position];
        int srcPos = i + 1 - position;
        if (srcPos < 0) {
          srcPos = 0;
        }
        System.arraycopy(src, srcPos, dest, 0, position);
        String ascii = "";
        try {
          ascii = new String(dest, format.getCharset());
        } catch (UnsupportedEncodingException e) {
          ascii = "Exception: " + e.getMessage();
        }
        stringBuilder.append("__________");
        stringBuilder.append(filter(ascii));
      }
    }

    return stringBuilder.toString();
  }

  private static String filter(String src)
  {
    Pattern pattern = Pattern.compile("\\p{Cntrl}");
    return pattern.matcher(src).replaceAll("*");
  }
}



