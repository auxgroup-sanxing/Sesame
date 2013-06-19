package com.sanxing.sesame.logging.dao;

import java.sql.Timestamp;

import com.sanxing.sesame.logging.constants.LogState;

public class LogBean
    extends BaseBean
{
    private static final long serialVersionUID = 7993539740276309124L;

    private Long serialNumber;

    private Timestamp startTime;

    private Timestamp updateTime;

    private String state = LogState.STATE_ONGOING;

    private String exceptionMessage;

    private String serviceName;

    private String operationName;

    private String transactionCode;

    private String channel;

    private String content;

    private String stage;

    private long expireTime;

    private Long count;

    private boolean callout = false;

    @Override
    public String toString()
    {
        return "LogBean [channel=" + channel + ", content=" + content + ", count=" + count + ", exceptionMessage="
            + exceptionMessage + ", expireTime=" + expireTime + ", operationName=" + operationName + ", serialNumber="
            + serialNumber + ", serviceName=" + serviceName + ", stage=" + stage + ", startTime=" + startTime
            + ", state=" + state + ", transactionCode=" + transactionCode + ", updateTime=" + updateTime + "]";
    }

    public String getContent()
    {
        return content;
    }

    public void setContent( String content )
    {
        this.content = content;
    }

    public boolean isCallout()
    {
        return callout;
    }

    public void setCallout( boolean callout )
    {
        this.callout = callout;
    }

    public Long getCount()
    {
        return count;
    }

    public void setCount( Long count )
    {
        this.count = count;
    }

    public Timestamp getStartTime()
    {
        return startTime;
    }

    public void setStartTime( Timestamp startTime )
    {
        this.startTime = startTime;
    }

    public Timestamp getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime( Timestamp updateTime )
    {
        this.updateTime = updateTime;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName( String serviceName )
    {
        this.serviceName = serviceName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName( String operationName )
    {
        this.operationName = operationName;
    }

    public String getTransactionCode()
    {
        return transactionCode;
    }

    public void setTransactionCode( String transactionCode )
    {
        this.transactionCode = transactionCode;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel( String channel )
    {
        this.channel = channel;
    }

    public long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime( long expireTime )
    {
        this.expireTime = expireTime;
    }

    public String getStage()
    {
        return stage;
    }

    public void setStage( String stage )
    {
        this.stage = stage;
    }

    public Long getSerialNumber()
    {
        return serialNumber;
    }

    public void setSerialNumber( Long serialNumber )
    {
        this.serialNumber = serialNumber;
    }

    public String getState()
    {
        return state;
    }

    public void setState( String state )
    {
        this.state = state;
    }

    public String getExceptionMessage()
    {
        return exceptionMessage;
    }

    public void setExceptionMessage( String exceptionMessage )
    {
        this.exceptionMessage = exceptionMessage;
    }
}