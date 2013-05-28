package com.sanxing.sesame.wtc.sop;

import com.sanxing.sesame.wtc.output.SOPOutputter;
import java.io.PrintStream;
import java.nio.ByteBuffer;

public class SOPFault
{
    private String errObj;

    private String errNo;

    private String errCode;

    private String errMsg;

    public String getErrObj()
    {
        return this.errObj;
    }

    public void setErrObj( String errObj )
    {
        this.errObj = errObj;
    }

    public String getErrNo()
    {
        return this.errNo;
    }

    public void setErrNo( String errNo )
    {
        this.errNo = errNo;
    }

    public String getErrCode()
    {
        return this.errCode;
    }

    public void setErrCode( String errCode )
    {
        this.errCode = errCode;
    }

    public String getErrMsg()
    {
        return this.errMsg;
    }

    public void setErrMsg( String errMsg )
    {
        this.errMsg = errMsg;
    }

    public byte[] encode()
    {
        int capacity = 1024;
        ByteBuffer buf = ByteBuffer.allocate( capacity );
        SOPUtil.putField( buf, this.errObj );
        SOPUtil.putField( buf, this.errNo );
        SOPUtil.putField( buf, this.errCode );
        SOPUtil.putField( buf, this.errMsg );
        int length = buf.position();
        buf.flip();
        byte[] result = new byte[length];
        System.arraycopy( buf.array(), 0, result, 0, length );
        return result;
    }
}
