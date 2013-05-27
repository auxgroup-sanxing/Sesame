package com.sanxing.sesame.wtc.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomDriver;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

public class SOPHeader
{
  private String macbranch;
  private String channelfrom;
  private String channelto;
  private String identifier;
  private ArrayList<SOPOperation> operations;

  public String getMacbranch()
  {
    return this.macbranch;
  }

  public void setMacbranch(String macbranch) {
    this.macbranch = macbranch;
  }

  public String getChannelfrom() {
    return this.channelfrom;
  }

  public void setChannelfrom(String channelfrom) {
    this.channelfrom = channelfrom;
  }

  public String getChannelto() {
    return this.channelto;
  }

  public void setChannelto(String channelto) {
    this.channelto = channelto;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public ArrayList<SOPOperation> getOperations() {
    return this.operations;
  }

  public void setOperations(ArrayList<SOPOperation> operations) {
    this.operations = operations;
  }

  public void addOperation(SOPOperation operation) {
    this.operations.add(operation);
  }

  public void delOperation(String code) {
    for (Iterator it = this.operations.iterator(); it.hasNext(); ) {
      String keyname = ((SOPOperation)it.next()).getCode();
      if ((keyname != null) && (keyname.equalsIgnoreCase(code)))
        it.remove();
    }
  }

  public int size()
  {
    return this.operations.size();
  }

  public String toString()
  {
    return "SOPHeader [macbranch=" + this.macbranch + ", channelfrom=" + 
      this.channelfrom + ", channelto=" + this.channelto + ", identifier=" + 
      this.identifier + ", operations=" + this.operations.size() + "]\n";
  }

  public static void main(String[] args) {
    try {
      XStream stream = new XStream(new JDomDriver());
      stream.alias("header", SOPHeader.class);
      stream.alias("transaction", SOPOperation.class);

      SOPOperation operation = new SOPOperation();
      operation.setCode("3009");
      operation.setEncrypt("yes");
      operation.setPinflag("flag");
      operation.setPinseed("seed");
      operation.setService("service");

      ArrayList transactions = new ArrayList();
      transactions.add(operation);

      SOPHeader header = new SOPHeader();
      header.setChannelfrom("230");
      header.setChannelto("810");
      header.setIdentifier("230");
      header.setMacbranch("9230");
      header.setOperations(transactions);
      stream.useAttributeFor("code", String.class);
      stream.useAttributeFor("encrypt", String.class);

      System.out.println(stream.toXML(header));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}



