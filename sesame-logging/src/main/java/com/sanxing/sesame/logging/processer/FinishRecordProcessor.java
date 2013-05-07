package com.sanxing.sesame.logging.processer;

import java.util.Date;

import com.sanxing.sesame.logging.FinishRecord;
import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.handlers.DataBaseHandler;
import com.sanxing.sesame.logging.handlers.LuceneHandler;
import com.sanxing.sesame.logging.handlers.MonitorQueueHandler;
import com.sanxing.sesame.logging.util.Utils;

public class FinishRecordProcessor
    extends RecordProcessor
{
    @Override
    public LogBean parse( Object o )
    {
        if ( o instanceof FinishRecord )
        {
            FinishRecord record = (FinishRecord) o;
            LogBean log = new LogBean();
            Date date = new Date();
            log.setSerialNumber( Long.valueOf( record.getSerial() ) );
            log.setStartTime( Utils.dateToTimeStamp( date ) );
            log.setUpdateTime( Utils.dateToTimeStamp( date ) );
            log.setState( "0" );
            log.setStage( record.getStage() );
            log.setServiceName( record.getServiceName() );
            log.setChannel( record.getChannel() );
            log.setOperationName( record.getOperationName() );
            log.setTransactionCode( record.getAction() );
            return log;
        }
        return null;
    }

    @Override
    public void register()
    {
        handlers.add( new DataBaseHandler() );
        handlers.add( new LuceneHandler() );
        handlers.add( new MonitorQueueHandler() );
    }
}