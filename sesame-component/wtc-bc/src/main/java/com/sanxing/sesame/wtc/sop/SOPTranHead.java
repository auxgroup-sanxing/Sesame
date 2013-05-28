package com.sanxing.sesame.wtc.sop;

public class SOPTranHead
{
    private static final int[] FIELDS_SIZE = { 4, 2, 1, 4, 2, 2, 2, 12, 8, 8, 16, 1, 2 };

    private static final String[] FIELDS_DESC = { "交易代码", "交易子码", "交易模式", "交易序号", "本交易包长度", "系统偏移1", "系统偏移2", "前台流水号",
        "前台日期", "授权柜员", "授权密码", "授权柜员有无卡标志", "授权柜员卡序号" };

    private String tranCode;

    private String ctranCode;

    private String mode;

    private int sequence;

    private short length;

    private short offsetA;

    private short offsetB;

    private String serial;

    private String date;

    private String teller;

    private String password;

    private String flag;

    private String cardIndex;

    public void decode( byte[] bytes )
    {
        int pos = 0;
        int index = 0;

        this.tranCode = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.ctranCode = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.mode = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.sequence = SOPUtil.getInt( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.length = SOPUtil.getShort( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.offsetA = SOPUtil.getShort( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.offsetB = SOPUtil.getShort( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.serial = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.date = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.teller = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.password = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.flag = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.cardIndex = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        sb.append( "\n#交易数据头:\n" );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.tranCode ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.ctranCode ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.mode ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.sequence ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.length ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.offsetA ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.offsetB ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.serial ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.date ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.teller ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.password ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.flag ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.cardIndex ) );
        return sb.toString();
    }

    public String getTranCode()
    {
        return this.tranCode;
    }

    public void setTranCode( String tranCode )
    {
        this.tranCode = tranCode;
    }
}
