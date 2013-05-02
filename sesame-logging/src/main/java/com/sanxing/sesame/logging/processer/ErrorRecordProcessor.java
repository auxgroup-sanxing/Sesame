package com.sanxing.sesame.logging.processer;

import com.sanxing.sesame.logging.ErrorRecord;
import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.handlers.DataBaseHandler;
import com.sanxing.sesame.logging.handlers.LuceneHandler;
import com.sanxing.sesame.logging.handlers.MonitorQueueHandler;
import com.sanxing.sesame.logging.util.Utils;
import java.util.Date;
import java.util.List;

public class ErrorRecordProcessor extends RecordProcessor {
	public LogBean parse(Object o) {
		if (o instanceof ErrorRecord) {
			ErrorRecord record = (ErrorRecord) o;
			LogBean log = new LogBean();
			Date date = new Date();
			log.setSerialNumber(Long.valueOf(record.getSerial()));
			log.setStartTime(Utils.dateToTimeStamp(date));
			log.setUpdateTime(Utils.dateToTimeStamp(date));
			log.setState("1");
			log.setStage(record.getStage());
			Exception exception = record.getException();
			if (exception != null) {
				log.setExceptionMessage(exception.getMessage());
			}
			return log;
		}
		return null;
	}

	public void register() {
		this.handlers.add(new DataBaseHandler());
		this.handlers.add(new LuceneHandler());
		this.handlers.add(new MonitorQueueHandler());
	}
}