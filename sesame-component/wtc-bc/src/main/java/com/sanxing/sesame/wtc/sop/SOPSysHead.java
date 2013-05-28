package com.sanxing.sesame.wtc.sop;

import com.sanxing.sesame.wtc.Mac;
import com.sanxing.sesame.wtc.config.SOPConfig;
import com.sanxing.sesame.wtc.config.SOPHeader;
import com.sanxing.sesame.wtc.config.SOPOperation;
import com.sanxing.sesame.wtc.output.SOPOutputter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOPSysHead
{
    private static final Logger LOG = LoggerFactory.getLogger( SOPSysHead.class );

    private static final int[] FIELDS_SIZE = { 2, 16, 4, 16, 3, 3, 1, 1, 1, 9, 1, 2, 1, 4, 3, 20, 20 };

    private static final String[] FIELDS_DESC = { "数据包长度", "报文MAC", "MAC机构号", "PIN种子", "渠道来源", "渠道去向", "加密标志", "PIN标志",
        "组合标志", "主机服务名", "信息结束标志", "报文序号", "校验标志", "密钥版本号", "系统标识符", "密码偏移量", "帐号偏移量" };

    private short size;

    private String mac;

    private String macBranch;

    private String channelFrom;

    private String channelTo;

    private String composite;

    private String endFlag;

    private short sequence;

    private String verify;

    private int version;

    private String identifier;

    private byte[] passwdOffset;

    private String acctOffset;

    private String pinSeed;

    private String encrypt;

    private String pinFlag;

    private String service;

    private byte[] bytes;

    private SOPConfig config = SOPConfig.getInstance();

    public SOPSysHead()
    {
        init();
    }

    public SOPSysHead( String code )
    {
        init( code );
    }

    private void init()
    {
        SOPHeader header = this.config.getHeader();

        this.macBranch = header.getMacbranch();
        this.channelFrom = header.getChannelfrom();
        this.channelTo = header.getChannelto();
        this.identifier = header.getIdentifier();
        this.mac = "01234567890123456";
        this.encrypt = "1";
        this.pinFlag = "0";
        this.pinSeed = "01234567890123456";
        this.service = "000000000";

        this.composite = "0";
        this.endFlag = "0";
        this.sequence = 0;
        this.verify = "0";
        this.version = 0;
        this.passwdOffset = new byte[20];
        this.acctOffset = "01234567890123456789";
    }

    public void init( String code )
    {
        init();
        SOPOperation operation = this.config.getOperation( code );
        if ( operation != null )
        {
            this.encrypt = operation.getEncrypt();
            this.pinFlag = operation.getPinflag();
            this.pinSeed = operation.getPinseed();
            this.service = operation.getService();
        }
    }

    private byte[] encode()
    {
        return encode( true );
    }

    private byte[] encodeNoMAC()
    {
        return encode( false );
    }

    private byte[] encode( boolean withMAC )
    {
        ByteBuffer buf;
        if ( withMAC )
        {
            int length = 107;
            buf = ByteBuffer.allocate( length );
            buf.order( ByteOrder.BIG_ENDIAN );
            buf.putShort( this.size );
            SOPUtil.putFixField( buf, this.mac, 16 );
        }
        else
        {
            int length = 89;

            buf = ByteBuffer.allocate( length );
            buf.order( ByteOrder.BIG_ENDIAN );
        }
        SOPUtil.putFixField( buf, this.macBranch, 4 );
        SOPUtil.putFixField( buf, this.pinSeed, 16 );
        SOPUtil.putFixField( buf, this.channelFrom, 3 );
        SOPUtil.putFixField( buf, this.channelTo, 3 );
        SOPUtil.putFixField( buf, this.encrypt, 1 );
        SOPUtil.putFixField( buf, this.pinFlag, 1 );
        SOPUtil.putFixField( buf, this.composite, 1 );
        SOPUtil.putFixField( buf, this.service, 9 );
        SOPUtil.putFixField( buf, this.endFlag, 1 );
        buf.putShort( this.sequence );
        SOPUtil.putFixField( buf, this.verify, 1 );
        buf.putInt( this.version );
        SOPUtil.putFixField( buf, this.identifier, 3 );

        for ( int index = 0; index < 20; index++ )
        {
            if ( index < this.passwdOffset.length )
                buf.put( this.passwdOffset[index] );
            else
            {
                buf.put( (byte) 0 );
            }
        }

        SOPUtil.putFixField( buf, this.acctOffset, 20 );

        buf.flip();
        return buf.array();
    }

    public boolean isEncrypt()
    {
        return ( this.encrypt != null ) && ( this.encrypt.equalsIgnoreCase( "1" ) );
    }

    public byte[] encode( byte[] body )
    {
        this.size = ( (short) ( 107 + body.length ) );

        if ( !isEncrypt() )
        {
            return encode();
        }

        byte[] bytesNoMac = encodeNoMAC();
        int length = bytesNoMac.length;
        int macSize = body.length + length;
        byte[] bytesMac = new byte[macSize];
        int destPos = 0;

        System.arraycopy( bytesNoMac, 0, bytesMac, destPos, length );
        destPos += length;
        System.arraycopy( body, 0, bytesMac, destPos, body.length );
        LOG.debug( "WTC:bytes to MAC: " );
        LOG.debug( SOPOutputter.format( bytesMac ) );

        byte[] mac = new Mac().mac( bytesMac );
        this.mac = new String( mac );

        int headSize = 107;
        ByteBuffer buf = ByteBuffer.allocate( headSize );
        buf.order( ByteOrder.BIG_ENDIAN );
        buf.putShort( this.size );
        buf.put( mac );
        buf.put( bytesNoMac );
        buf.flip();
        return buf.array();
    }

    public void decode( byte[] bytes )
    {
        int pos = 0;
        int index = 0;
        int length = bytes.length;
        this.bytes = new byte[length];
        System.arraycopy( bytes, 0, this.bytes, 0, length );

        this.size = SOPUtil.getShort( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.mac = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.macBranch = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.pinSeed = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.channelFrom = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.channelTo = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.encrypt = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.pinFlag = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.composite = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.service = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.endFlag = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.sequence = SOPUtil.getShort( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.verify = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.version = SOPUtil.getInt( bytes, pos );
        pos += FIELDS_SIZE[( index++ )];

        this.identifier = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.passwdOffset = SOPUtil.getBytes( bytes, pos, FIELDS_SIZE[index] );
        pos += FIELDS_SIZE[( index++ )];

        this.acctOffset = SOPUtil.getField( bytes, pos, FIELDS_SIZE[index] );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        sb.append( "\n#系统信息头:\n" );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.size ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.mac ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.macBranch ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.pinSeed ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.channelFrom ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.channelTo ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.encrypt ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.pinFlag ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.composite ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.service ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.endFlag ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.sequence ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.verify ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.version ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.identifier ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], new String( this.passwdOffset ) ) );
        sb.append( SOPUtil.format( FIELDS_DESC[( index++ )], this.acctOffset ) );
        return sb.toString();
    }

    public String getMac()
    {
        return this.mac;
    }

    public void setMac( String mac )
    {
        this.mac = mac;
    }

    public String getEncrypt()
    {
        return this.encrypt;
    }

    public void setEncrypt( String encrypt )
    {
        this.encrypt = encrypt;
    }

    public byte[] getPasswdOffset()
    {
        return this.passwdOffset;
    }

    public void setPasswdOffset( byte[] passwdOffset )
    {
        this.passwdOffset = passwdOffset;
    }

    public String getPinSeed()
    {
        return this.pinSeed;
    }

    public void setPinSeed( String pinSeed )
    {
        this.pinSeed = pinSeed;
    }

    public String getPinFlag()
    {
        return this.pinFlag;
    }

    public void setPinFlag( String pinFlag )
    {
        this.pinFlag = pinFlag;
    }

    public String getChannelFrom()
    {
        return this.channelFrom;
    }

    public void setChannelFrom( String channelFrom )
    {
        this.channelFrom = channelFrom;
    }

    public String getChannelTo()
    {
        return this.channelTo;
    }

    public void setChannelTo( String channelTo )
    {
        this.channelTo = channelTo;
    }
}
