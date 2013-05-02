package com.sanxing.sesame.core.jdbc;

import com.sanxing.sesame.core.BaseServer;
import java.sql.SQLException;
import javax.naming.Context;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;

public class DBCPProvider implements DataSourceProvider {
	private static Logger LOG = LoggerFactory.getLogger(DBCPProvider.class);
	private BasicDataSource datasource;

	public void provide(BaseServer server, DataSourceInfo dsInfo) {
		try {
			Element resourceEl = dsInfo.getAppInfo();

			String driver = resourceEl.getChildText("driver-class");
			String url = resourceEl.getChildText("url");

			this.datasource = new BasicDataSource();

			this.datasource.setDriverClassName(driver);
			this.datasource.setUrl(url);
			this.datasource.setUsername(resourceEl.getChildText("username"));
			this.datasource.setPassword(resourceEl.getChildText("password"));
			this.datasource.setMaxActive(Integer.parseInt(resourceEl
					.getChildText("max-active")));
			this.datasource.setMaxIdle(Integer.parseInt(resourceEl
					.getChildText("max-idle")));
			this.datasource.setMaxWait(Long.parseLong(resourceEl
					.getChildText("max-wait")));
			this.datasource.setInitialSize(Integer.parseInt(resourceEl
					.getChildText("initial-size")));
			Context context = server.getNamingContext();
			String name = dsInfo.getJndiName();

			context.bind(name, this.datasource);
		} catch (Exception e) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(e.getMessage(), e);
			} else
				LOG.error(e.getMessage());
		}
	}

	public void release() {
		try {
			this.datasource.close();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		BasicDataSource datasource = new BasicDataSource();

		datasource.setDriverClassName("");
		datasource.setUrl("");
		datasource.setUsername("db2inst1");
		datasource.setPassword("db2inst1");
	}
}