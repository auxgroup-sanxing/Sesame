package com.sanxing.sesame.core.jms;

import com.sanxing.sesame.core.BaseServer;

public abstract interface JMSProvider {
	public abstract void prepare(BaseServer paramBaseServer,
			JMSServiceInfo paramJMSServiceInfo);

	public abstract void release();
}