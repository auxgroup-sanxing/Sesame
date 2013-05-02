package com.sanxing.sesame.logging.processer;

import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.handlers.LogHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class RecordProcessor implements Processor {
	protected List<LogHandler> handlers = new ArrayList();
	protected LogBean bean;

	public void process(Object o) {
		register();

		this.bean = parse(o);

		for (Iterator it = this.handlers.iterator(); it.hasNext();) {
			LogHandler handler = (LogHandler) it.next();
			handler.handle(this.bean);
		}
	}

	public abstract void register();

	public abstract LogBean parse(Object paramObject);
}