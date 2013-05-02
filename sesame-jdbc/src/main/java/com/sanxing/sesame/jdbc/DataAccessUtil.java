package com.sanxing.sesame.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUtil {
	private static final Logger LOG = LoggerFactory.getLogger(DataAccessUtil.class);

	public static void closeConnection(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (Throwable t) {
			logFault(t);
		}
	}

	public static void closeStatement(Statement statement) {
		try {
			if (statement != null)
				statement.close();
		} catch (Throwable t) {
			logFault(t);
		}
	}

	public static void closeResultSet(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (Throwable t) {
			logFault(t);
		}
	}

	private static void logFault(Throwable t) {
		try {
			LOG.error(t.getMessage(), t);
		} catch (Throwable localThrowable) {
		}
	}

	public static void handleFault(Throwable t) {
		if (t != null) {
			if (t instanceof DataAccessException) {
				DataAccessException dae = (DataAccessException) t;
				throw dae;
			}
			if (t instanceof SQLException)
				throw new DataAccessException(t);
			if (t instanceof RuntimeException) {
				RuntimeException re = (RuntimeException) t;
				throw re;
			}
			if (t instanceof Error) {
				Error err = (Error) t;
				throw err;
			}
			throw new RuntimeException(t);
		}

		throw new NullPointerException("fault is null");
	}
}