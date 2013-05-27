package com.sanxing.sesame.wtc;

import com.union.HsmAPI.gdHsmAPI;
import java.io.File;

public class Mac
{
  private String keyName;
  private String cfgName;

  public Mac()
  {
    this.keyName = "IC.00009230.zak";
    this.cfgName = "HsmSvr.CFG";
  }

  public byte[] mac(byte[] bytes) {
    try {
      String gdHsmPath = this.cfgName;
      String path = getClass().getClassLoader()
        .getResource(gdHsmPath).getPath();
      long start = System.currentTimeMillis();
      gdHsmAPI tt = new gdHsmAPI(new File(path).getParent() + 
        File.separator);
      tt.Init();
      long end = System.currentTimeMillis();
      System.out.println("init elapse: " + (end - start));

      start = System.currentTimeMillis();
      String mac = tt.UnionGenMac(this.keyName, bytes.length, bytes);
      end = System.currentTimeMillis();
      System.out.println("generate elapse: " + (end - start));

      return mac.getBytes();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public boolean verify(byte[] content, byte[] mac)
  {
    try
    {
      String gdHsmPath = this.cfgName;
      String path = getClass().getClassLoader()
        .getResource(gdHsmPath).getPath();

      long start = System.currentTimeMillis();
      gdHsmAPI tt = new gdHsmAPI(new File(path).getParent() + 
        File.separator);
      tt.Init();
      long end = System.currentTimeMillis();
      System.out.println("init elapse: " + (end - start));

      start = System.currentTimeMillis();
      int rtn = tt.UnionVerifyMac(this.keyName, content.length, content, 
        new String(mac));
      end = System.currentTimeMillis();
      System.out.println("verify elapse:" + (end - start));

      if (rtn >= 0) {
        return true;
      }
      return false;
    }
    catch (Exception e) {
      e.printStackTrace();
    }return false;
  }

  public boolean store(String id, String keyvalue, String keychk)
  {
    try
    {
      String gdHsmPath = this.cfgName;
      String path = getClass().getClassLoader()
        .getResource(gdHsmPath).getPath();

      long start = System.currentTimeMillis();
      gdHsmAPI tt = new gdHsmAPI(new File(path).getParent() + 
        File.separator);
      tt.Init();
      long end = System.currentTimeMillis();
      System.out.println("init elapse: " + (end - start));

      start = System.currentTimeMillis();
      int rtn = tt.UnionStoreKey(id, keyvalue, keychk);
      end = System.currentTimeMillis();
      System.out.println("verify elapse:" + (end - start));
      if (rtn >= 0) {
        return true;
      }
      return false;
    }
    catch (Exception e) {
      e.printStackTrace();
    }return false;
  }

  public String getKeyName()
  {
    return this.keyName;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public static void main(String[] args) {
    Mac mac = new Mac();
    String hexString = "39 32 33 30 38 35 30 35 33 31 37 38 31 32 38 31 35 33 39 39 32 33 30 38 31 30 31 31 30 30 30 30 30 30 30 30 30 30 30 00 00 30 00 00 00 00 32 33 30 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 30 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36 37 38 39 39 37 37 32 20 20 20 20 32 30 31 31 30 37 31 31 00 02 36 FE 33 35 30 30 30 30 35 31 34 35 37 35 00 00 41 41 41 41 41 41 41 06 4F 39 37 37 32 31 0A 68 6F 73 74 73 65 72 69 61 6C 01 3F 36 37 32 31 39 39 46 31 38 30 34 30 30 30 30 30 30 30 31 38 36 31 30 30 34 44 41 39 46 37 39 30 41 30 30 30 30 30 30 30 30 30 31 32 33 35 39 34 32 39 42 33 32 30 30 14 30 45 31 38 36 46 35 42 42 43 36 39 46 43 39 46 33 30 33 30 04 30 2E 30 30 04 31 2E 32 33 05 36 30 30 31 36";
    byte[] input = ByteUtil.hexStringToBytes(hexString.replaceAll(" ", ""));

    long start = System.currentTimeMillis();
    byte[] result = mac.mac(input);
    long end = System.currentTimeMillis();
    System.out.println(new String(result));
    System.out.println("total mac elapse: " + (end - start) + "\n");
  }
}



