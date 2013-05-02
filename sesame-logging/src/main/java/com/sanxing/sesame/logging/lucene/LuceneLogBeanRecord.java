package com.sanxing.sesame.logging.lucene;

import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneLogBeanRecord extends LuceneRecord {
	private static final Logger LOG = LoggerFactory
			.getLogger(LuceneLogBeanRecord.class);

	private static final String[] fields = { "serialNumber", "state",
			"serviceName", "operationName", "transactionCode", "channel",
			"content" };

	public LuceneLogBeanRecord() {
	}

	public LuceneLogBeanRecord(LogBean bean) {
		if (bean == null) {
			return;
		}
		for (int i = 0; i < fields.length; ++i) {
			String name = fields[i];
			Object o = Utils.getComponentByName(bean, name);
			if (o != null) {
				String value = String.valueOf(o);
				LuceneColumn column = new LuceneColumn(name, value, true);
				addField(column);
				LOG.debug("name=" + name + "; value=" + value);
			}
		}
	}
}