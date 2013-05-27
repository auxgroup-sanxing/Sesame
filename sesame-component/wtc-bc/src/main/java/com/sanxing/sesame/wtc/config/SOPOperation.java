package com.sanxing.sesame.wtc.config;

public class SOPOperation
{
  private String code;
  private String encrypt;
  private String pinseed;
  private String pinflag;
  private String service;

  public String getCode()
  {
    return this.code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getEncrypt() {
    return this.encrypt;
  }

  public void setEncrypt(String encrypt) {
    this.encrypt = encrypt;
  }

  public String getPinseed() {
    return this.pinseed;
  }

  public void setPinseed(String pinseed) {
    this.pinseed = pinseed;
  }

  public String getPinflag() {
    return this.pinflag;
  }

  public void setPinflag(String pinflag) {
    this.pinflag = pinflag;
  }

  public String getService() {
    return this.service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String toString()
  {
    return "SOPOperation [code=" + this.code + ", encrypt=" + this.encrypt + 
      ", pinseed=" + this.pinseed + ", pinflag=" + this.pinflag + 
      ", service=" + this.service + "]\n";
  }
}



