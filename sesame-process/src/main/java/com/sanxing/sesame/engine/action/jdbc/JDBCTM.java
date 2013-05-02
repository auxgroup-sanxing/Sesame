package com.sanxing.sesame.engine.action.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCTM implements TXManager {
	private Connection conn;
	private static Logger LOG = LoggerFactory.getLogger(JDBCTM.class);

	private int status = -1;

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public Connection getConnection() {
		if (this.conn == null) {
			throw new RuntimeException("tx not started");
		}
		return this.conn;
	}

	public void begin() {
		if (this.status == -1) {
			try {
				this.conn.setAutoCommit(false);
				this.status = 0;
			} catch (SQLException e) {
				LOG.error("Begin transaction failure", e);
			}
		} else if (this.status == 0) {
			LOG.warn(this + "already begin");
		} else if (this.status == 1) {
			LOG.warn(this + " already commited");
		} else if (this.status == 2)
			LOG.warn(this + " already rollbacked");
	}

	public void commit() {
		if (this.status == 0)
			try {
				this.conn.commit();
			} catch (SQLException e) {
				LOG.error("commit failure", e);
			}
		else if (this.status == 1)
			LOG.warn(this + " already commited");
		else if (this.status == 2)
			LOG.warn(this + " already rollbacked");
		else
			LOG.warn("unkown status");
	}

	public void rollback() {
		if (this.status == 0)
			try {
				this.conn.rollback();
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		else if (this.status == 1)
			LOG.warn(this + " already commited");
		else if (this.status == 2)
			LOG.warn(this + " already rollbacked");
		else
			LOG.warn("unkown status");
	}

	public int getStatus() {
		return this.status;
	}
}