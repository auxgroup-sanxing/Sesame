package com.sanxing.sesame.logging.processer;

import com.sanxing.sesame.logging.BufferRecord;
import com.sanxing.sesame.logging.ErrorRecord;
import com.sanxing.sesame.logging.FinishRecord;
import com.sanxing.sesame.logging.XObjectRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessorFactory {
	private static ProcessorFactory instance = null;

	private static final Logger LOG = LoggerFactory.getLogger(ProcessorFactory.class);

	public static ProcessorFactory getInstance() {
		if (instance == null) {
			synchronized (ProcessorFactory.class) {
				if (instance == null) {
					instance = new ProcessorFactory();
				}
			}
		}
		return instance;
	}

	public Processor produce(Object o) {
		if (o == null) {
			return null;
		}

		if (o instanceof BufferRecord)
			return new BufferRecordProcessor();
		if (o instanceof XObjectRecord)
			return new XObjectRecordProcessor();
		if (o instanceof FinishRecord)
			return new FinishRecordProcessor();
		if (o instanceof ErrorRecord) {
			return new ErrorRecordProcessor();
		}
		LOG.debug("Unsupported type!");
		return null;
	}
}