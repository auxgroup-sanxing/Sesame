package com.sanxing.sesame.logging.processer;

import java.util.Date;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;

import com.sanxing.sesame.logging.XObjectRecord;
import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.handlers.DataBaseHandler;
import com.sanxing.sesame.logging.handlers.LuceneHandler;
import com.sanxing.sesame.logging.util.Utils;

public class XObjectRecordProcessor
    extends RecordProcessor
{
    @Override
    public LogBean parse( Object o )
    {
        if ( o instanceof XObjectRecord )
        {
            XObjectRecord record = (XObjectRecord) o;
            LogBean log = new LogBean();
            Date date = new Date();
            log.setSerialNumber( Long.valueOf( record.getSerial() ) );
            log.setStartTime( Utils.dateToTimeStamp( date ) );
            log.setUpdateTime( Utils.dateToTimeStamp( date ) );
            String stage = record.getStage();
            log.setStage( stage );
            Document doc = record.getDocument();
            XMLOutputter outputter = new XMLOutputter();
            String xmlStr = outputter.outputString( doc.getRootElement() );
            log.setContent( xmlStr );
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

    @Override
    public void register()
    {
        handlers.add( new DataBaseHandler() );
        handlers.add( new LuceneHandler() );
    }
}