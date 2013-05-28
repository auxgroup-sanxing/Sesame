package com.sanxing.sesame.wtc.sop;

import com.sanxing.sesame.wtc.ByteUtil;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SOPResponse
{
    private static final int[] FIELDS_SIZE = { 4, 4, 8, 4, 12, 2, 7 };

    private static final String[] FIELDS_DESC = { "交易代码", "联动交易码", "交易日期", "交易时间", "柜员流水号", "出错交易序号", "错误代号" };

    private SOPSysHead head;

    private String tranCode;

    private String coTranCode;

    private String date;

    private int time;

    private String serial;

    private short sequence;

    private String errCode;

    private byte[] body;

    public SOPResponse()
    {
        init();
    }

    public SOPResponse( String errCode, String errMsg )
    {
        init();

        SOPFault fault = new SOPFault();
        fault.setErrObj( "ERR000" );
        fault.setErrNo( "" );
        fault.setErrCode( errCode );
        fault.setErrMsg( errMsg );
        byte[] body = fault.encode();

        SOPSysHead syshead = new SOPSysHead();

        setErrCode( errCode );
        setBody( body );
        setHead( syshead );
    }

    private void init()
    {
        this.tranCode = "0000";
        this.errCode = "0000000";
        this.date = getDate();
        this.time = getTime();

        this.coTranCode = "0000";
        this.serial = "000000000000";
        this.sequence = 0;
    }

    public byte[] encode()
    {
        byte[] phead = encodePubHead();
        byte[] content = ByteUtil.cancat( phead, this.body );
        this.head.init( this.tranCode );
        this.head.setPasswdOffset( new byte[20] );
        this.head.setPinFlag( "" );
        this.head.setPinFlag( "" );
        byte[] syshead = this.head.encode( content );
        byte[] result = ByteUtil.cancat( syshead, content );
        return result;
    }

    public byte[] encodeBody()
    {
        byte[] phead = encodePubHead();
        byte[] content = ByteUtil.cancat( phead, this.body );
        return content;
    }

    public void decode( byte[] bytes )
    {
        int bodyLen = bytes.length - 107 - 41;
        if ( bodyLen < 0 )
        {
            throw new RuntimeException( "response package size is invalid." );
        }

        byte[] headBytes = new byte[107];
        byte[] pubBytes = new byte[41];
        int pos = 0;
        System.arraycopy( bytes, pos, headBytes, 0, 107 );
        pos += 107;
        System.arraycopy( bytes, pos, pubBytes, 0, 41 );
        pos += 41;
        this.head = new SOPSysHead();
        this.head.decode( headBytes );

        decodePubHead( pubBytes );
    }

    private byte[] encodePubHead()
    {
        int capacity = 1024;
        ByteBuffer buf = ByteBuffer.allocate( capacity );
        SOPUtil.putFixField( buf, this.tranCode, 4 );
        SOPUtil.putFixField( buf, this.coTranCode, 4 );
        SOPUtil.putFixField( buf, this.date, 8 );
        buf.putInt( this.time );
        SOPUtil.putFixField( buf, this.serial, 12 );
        buf.putShort( this.sequence );
        SOPUtil.putFixField( buf, this.errCode, 7 );
        int length = buf.position();
        buf.flip();
        byte[] result = new byte[length];
        System.arraycopy( buf.array(), 0, result, 0, length );
        return result;
    }

    private void decodePubHead( byte[] bytes )
    {
        int pos = 0;
        int index = 0;

        this.tranCode = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.coTranCode = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.date = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.time = SOPUtil.getInt( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.serial = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.sequence = SOPUtil.getShort( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.errCode = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
    }

    private String getDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd" );
        String date = sdf.format( new Date() );
        return date;
    }

    private int getTime()
    {
        Calendar obj = Calendar.getInstance();
        int hour = obj.get( 11 );
        int minute = obj.get( 12 );
        int second = obj.get( 13 );
        return hour * 10000 + minute * 100 + second;
    }

    public SOPSysHead getHead()
    {
        return this.head;
    }

    public void setHead( SOPSysHead head )
    {
        this.head = head;
    }

    public String getTranCode()
    {
        return this.tranCode;
    }

    public void setTranCode( String tranCode )
    {
        this.tranCode = tranCode;
    }

    public String getCoTranCode()
    {
        return this.coTranCode;
    }

    public void setCoTranCode( String coTranCode )
    {
        this.coTranCode = coTranCode;
    }

    public String getSerial()
    {
        return this.serial;
    }

    public void setSerial( String serial )
    {
        this.serial = serial;
    }

    public short getSequence()
    {
        return this.sequence;
    }

    public void setSequence( short sequence )
    {
        this.sequence = sequence;
    }

    public String getErrCode()
    {
        return this.errCode;
    }

    public void setErrCode( String errCode )
    {
        this.errCode = errCode;
    }

    public byte[] getBody()
    {
        return this.body;
    }

    public void setBody( byte[] body )
    {
        this.body = body;
    }

    public void setDate( String date )
    {
        this.date = date;
    }

    public void setTime( int time )
    {
        this.time = time;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        sb.append( "\n" );
        sb.append( this.head.toString() );
        sb.append( "\n#交易返回公共信息头:\n" );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.tranCode ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.coTranCode ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.date ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.time ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.serial ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.sequence ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.errCode ) );
        return sb.toString();
    }
}
