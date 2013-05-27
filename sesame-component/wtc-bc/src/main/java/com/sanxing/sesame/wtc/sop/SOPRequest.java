package com.sanxing.sesame.wtc.sop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOPRequest
{
  private static final Logger LOG = LoggerFactory.getLogger(SOPRequest.class);

  private SOPSysHead head = null;

  private SOPReqPublic pubHead = null;

  private SOPTranHead tranHead = null;
  private byte[] body;
  private byte[] content;

  public void decode(byte[] bytes, boolean withSysHead)
  {
    this.content = bytes;
    int bodyLen = bytes.length - 21 - 
      64;
    if (withSysHead) {
      bodyLen -= 107;
    }
    if (bodyLen <= 0) {
      throw new RuntimeException("request package size is invalid.");
    }
    this.body = new byte[bodyLen];

    byte[] headBytes = new byte[107];
    byte[] pubBytes = new byte[21];
    byte[] tranBytes = new byte[64];
    int pos = 0;
    if (withSysHead) {
      System.arraycopy(bytes, pos, headBytes, 0, 
        107);
      pos += 107;
    }
    System.arraycopy(bytes, pos, pubBytes, 0, 21);
    pos += 21;
    System.arraycopy(bytes, pos, tranBytes, 0, 
      64);
    pos += 64;
    System.arraycopy(bytes, pos, this.body, 0, bodyLen);
    this.head = new SOPSysHead();
    this.head.decode(headBytes);

    this.pubHead = new SOPReqPublic();
    this.pubHead.decode(pubBytes);

    this.tranHead = new SOPTranHead();
    this.tranHead.decode(tranBytes);
  }

  public void decode(byte[] bytes)
  {
    decode(bytes, true);
  }

  public void decodeNoSyshead(byte[] bytes) {
    decode(bytes, false);
  }

  public String toString()
  {
    String output = "\n";
    if (this.head != null) {
      output = output.concat(this.head.toString());
    }
    if (this.pubHead != null) {
      output = output.concat(this.pubHead.toString());
    }
    if (this.tranHead != null) {
      output = output.concat(this.tranHead.toString());
    }
    return output;
  }

  public SOPSysHead getHead() {
    return this.head;
  }

  public void setHead(SOPSysHead head) {
    this.head = head;
  }

  public SOPReqPublic getPubHead() {
    return this.pubHead;
  }

  public void setPubHead(SOPReqPublic pubHead) {
    this.pubHead = pubHead;
  }

  public SOPTranHead getTranHead() {
    return this.tranHead;
  }

  public void setTranHead(SOPTranHead tranHead) {
    this.tranHead = tranHead;
  }

  public byte[] getBody() {
    return this.body;
  }

  public void setBody(byte[] body) {
    this.body = body;
  }

  public byte[] getContent() {
    return this.content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }
}



