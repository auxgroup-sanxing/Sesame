package com.sanxing.sesame.core.jdbc;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.util.GetterUtil;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

public class BTMProvider implements DataSourceProvider {
	private static Logger LOG = LoggerFactory.getLogger(DBCPProvider.class);
	private static PoolingDataSource myDataSource;

	public void provide(BaseServer server, DataSourceInfo dsInfo) {
		Element configEle = dsInfo.getAppInfo();
		boolean useLRC = GetterUtil.getBoolean(
				configEle.getChildText("use-lrc"), false);
		String uniqueName = GetterUtil.getString(configEle
				.getChildText("unique-name"));
		int maxPoolSize = GetterUtil.getInteger(
				configEle.getChildText("max-pool-size"), 10);
		String driverClassName = GetterUtil.getString(configEle
				.getChildText("driver-class"));
		String url = GetterUtil.getString(configEle.getChildText("url"));
		String userName = GetterUtil.getString(configEle
				.getChildText("username"));
		String password = GetterUtil.getString(configEle
				.getChildText("password"));
		myDataSource = new PoolingDataSource();
		if (useLRC) {
			myDataSource
					.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
			myDataSource.getDriverProperties().setProperty("driverClassName",
					driverClassName);
			myDataSource.setAllowLocalTransactions(true);
			myDataSource.getDriverProperties().setProperty("url", url);
		} else {
			myDataSource.getDriverProperties().setProperty("URL", url);
			myDataSource.setClassName(driverClassName);
			myDataSource.setAllowLocalTransactions(true);
		}
		String name = dsInfo.getJndiName();
		myDataSource.setUniqueName(name);
		myDataSource.setMaxPoolSize(maxPoolSize);

		myDataSource.getDriverProperties().setProperty("user", userName);
		myDataSource.getDriverProperties().setProperty("password", password);
		Context context = server.getNamingContext();
		try {
			context.bind(name, myDataSource);

			BitronixTransactionManager btm = TransactionManagerServices
					.getTransactionManager();
			try {
				if (context.lookup("java:comp/UserTransaction") == null)
					context.bind("java:comp/UserTransaction", btm);
			} catch (NameNotFoundException e) {
				context.rebind("java:comp/UserTransaction", btm);
			}
		} catch (NamingException e) {
			if (LOG.isTraceEnabled())
				LOG.trace(e.getMessage(), e);
			else
				LOG.error(e.getMessage());
		}
	}

	public void release() {
		myDataSource.close();
	}
}