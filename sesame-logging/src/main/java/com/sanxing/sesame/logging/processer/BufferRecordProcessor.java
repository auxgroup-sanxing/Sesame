package com.sanxing.sesame.logging.processer;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.sanxing.sesame.logging.BufferRecord;
import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.handlers.DataBaseHandler;
import com.sanxing.sesame.logging.handlers.LuceneHandler;
import com.sanxing.sesame.logging.util.Utils;

public class BufferRecordProcessor
    extends RecordProcessor
{
    @Override
    public void register()
    {
        handlers.add( new DataBaseHandler() );
        handlers.add( new LuceneHandler() );
    }

    @Override
    public LogBean parse( Object o )
    {
        if ( o instanceof BufferRecord )
        {
            BufferRecord record = (BufferRecord) o;
            LogBean log = new LogBean();
            Date date = new Date();
            log.setSerialNumber( Long.valueOf( record.getSerial() ) );
            log.setStartTime( Utils.dateToTimeStamp( date ) );
            log.setUpdateTime( Utils.dateToTimeStamp( date ) );
            String stage = record.getStage();
            log.setStage( stage );
            try
            {
                log.setContent( new String( record.getBuffer(), record.getEncoding() ) );
            }
            catch ( UnsupportedEncodingException e )
            {
                e.printStackTrace();
            }
            log.setExpireTime( record.getExpireTime() );
            log.setServiceName( record.getServiceName() );
            log.setChannel( record.getChannel() );
            log.setOperationName( record.getOperationName() );
            log.setTransactionCode( record.getAction() );
            log.setCallout( record.isCallout() );
            return log;
        }
        return null;
    }
}