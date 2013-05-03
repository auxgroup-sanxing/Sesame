package com.sanxing.sesame.core.jdbc;

import com.sanxing.sesame.jdbc.SesameDataSource;
import com.sanxing.sesame.jdbc.SesameTransactionManager;
import com.sanxing.sesame.core.BaseServer;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

public class STMProvider implements DataSourceProvider {
	private static final Logger LOG = LoggerFactory.getLogger(STMProvider.class);
	private SesameDataSource sesameDataSource;

	public void provide(BaseServer server, DataSourceInfo dsInfo) {
		try {
			Element resourceEl = dsInfo.getAppInfo();
			String driver = resourceEl.getChildText("driver-class");
			String url = resourceEl.getChildText("url");
			BasicDataSource datasource = new BasicDataSource();
			datasource.setDriverClassName(driver);
			datasource.setUrl(url);
			datasource.setUsername(resourceEl.getChildText("username"));
			datasource.setPassword(resourceEl.getChildText("password"));
			datasource.setMaxActive(Integer.parseInt(resourceEl
					.getChildText("max-active")));
			datasource.setMaxIdle(Integer.parseInt(resourceEl
					.getChildText("max-idle")));
			datasource.setMaxWait(Long.parseLong(resourceEl
					.getChildText("max-wait")));
			datasource.setInitialSize(Integer.parseInt(resourceEl
					.getChildText("initial-size")));
			this.sesameDataSource = new SesameDataSource(datasource);
			Context context = server.getNamingContext();
			String name = dsInfo.getJndiName();
			context.bind(name, this.sesameDataSource);

			SesameTransactionManager tm = SesameTransactionManager
					.getInstance();
			tm.bindWithDataSource(this.sesameDataSource);
			try {
				if (context.lookup("java:comp/UserTransaction") == null) {
					context.createSubcontext("java:comp");
					context.rebind("java:comp/UserTransaction", tm);
				}
			} catch (NameNotFoundException e) {
				context.createSubcontext("java:comp");
				context.rebind("java:comp/UserTransaction", tm);
			}
		} catch (Exception e) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(e.getMessage(), e);
			} else
				LOG.error(e.getMessage());
		}
	}

	public void release() {
	}
}