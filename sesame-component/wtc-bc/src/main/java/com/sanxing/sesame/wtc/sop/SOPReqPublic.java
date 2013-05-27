package com.sanxing.sesame.wtc.sop;

public class SOPReqPublic
{
  private static final int[] FIELDS_SIZE = { 5, 4, 4, 8 };

  private static final String[] FIELDS_DESC = { "终端号", "城市代码", "机构代码", "交易柜员" };
  private String terminal;
  private String city;
  private String branch;
  private String teller;

  public void decode(byte[] bytes)
  {
    int pos = 0;
    int index = 0;

    this.terminal = SOPUtil.getField(bytes, pos, FIELDS_SIZE[index]);
    pos += FIELDS_SIZE[(index++)];

    this.city = SOPUtil.getField(bytes, pos, FIELDS_SIZE[index]);
    pos += FIELDS_SIZE[(index++)];

    this.branch = SOPUtil.getField(bytes, pos, FIELDS_SIZE[index]);
    pos += FIELDS_SIZE[(index++)];

    this.teller = SOPUtil.getField(bytes, pos, FIELDS_SIZE[index]);
  }

  public String getTerminal() {
    return this.terminal;
  }

  public void setTerminal(String terminal) {
    this.terminal = terminal;
  }

  public String getCity() {
    return this.city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getBranch() {
    return this.branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getTeller() {
    return this.teller;
  }

  public void setTeller(String teller) {
    this.teller = teller;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    int index = 0;
    sb.append("\n#交易公共信息头:\n");
    sb.append(SOPUtil.format(FIELDS_DESC[(index++)], this.terminal));
    sb.append(SOPUtil.format(FIELDS_DESC[(index++)], this.city));
    sb.append(SOPUtil.format(FIELDS_DESC[(index++)], this.branch));
    sb.append(SOPUtil.format(FIELDS_DESC[(index++)], this.teller));
    return sb.toString();
  }
}



