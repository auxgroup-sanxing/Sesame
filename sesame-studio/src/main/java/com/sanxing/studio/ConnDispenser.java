package com.sanxing.studio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

public class ConnDispenser {
	private static Logger logger = LoggerFactory.getLogger(ConnDispenser.class);

	private static DataSource datasource = null;

	public static Connection getConnection() throws Exception {
		if (datasource == null) {
			if (Configuration.useDataSource()) {
				Context initCtx = new InitialContext();
				datasource = (DataSource) initCtx.lookup(Configuration
						.getDataSource());
			} else {
				BasicDataSource ds = new BasicDataSource();
				ds.setDriverClassName(Configuration.getDriverClass());
				ds.setUrl(Configuration.getConnetionUrl());
				ds.setUsername(Configuration.getConnectionUser());
				ds.setPassword(Configuration.getConnectionPasswd());
				ds.setMaxActive(20);
				datasource = ds;
			}
		}
		return datasource.getConnection();
	}

	public static void closeDataSource() {
		if (datasource == null)
			return;
		try {
			if (datasource instanceof BasicDataSource)
				((BasicDataSource) datasource).close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		datasource = null;
	}

	public static ResultSet executeQuery(String sqlText, Connection conn)
			throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sqlText);
		stmt.close();
		return rs;
	}

	public static int executeDDL(Connection conn, String sqlText)
			throws Exception {
		Statement stmt = conn.createStatement();
		try {
			int result = stmt.executeUpdate(sqlText);

			return result;
		} finally {
			stmt.close();
		}
	}

	public static int executeDML(Connection conn, String sqlText)
			throws Exception {
		Statement stmt = conn.createStatement();
		try {
			int result = stmt.executeUpdate(sqlText);

			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			stmt.close();
		}
	}

	public static int getRecordCount(String tableName, String condition,
			Connection conn) throws SQLException {
		try {
			int result = 0;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM "
					+ tableName + "\n" + condition);

			if (rs.next())
				rs.getInt(1);
			rs.close();
			stmt.close();

			return result;
		} finally {
			conn.close();
		}
	}

	public static int getColumnMaxValue(String tableName, String columnName,
			Connection conn) throws SQLException {
		int result = -1;
		Statement stmt = conn.createStatement();
		String sql = "SELECT MAX(" + columnName + ") FROM " + tableName;
		System.out.println("get max value sql is:" + sql);
		ResultSet rs = stmt.executeQuery(sql);
		if (rs.next()) {
			result = rs.getInt(1);
		}

		rs.close();
		stmt.close();
		return result;
	}

	public static String getSQLInsert(DatabaseMetaData meta, String tableName,
			JSONObject object) throws SQLException {
		ArrayList fields = new ArrayList();
		ArrayList values = new ArrayList();
		ResultSet rs = meta.getColumns(null, null, tableName, null);

		while (rs.next()) {
			String colName = rs.getString("COLUMN_NAME");
			if (fields.indexOf(colName) > -1) {
				continue;
			}
			if (object.has(colName)) {
				fields.add(colName);
				String value;
				switch (rs.getInt("DATA_TYPE")) {
				case 1:
				case 12:
					value = object.optString(colName);
					values.add("'" + value.replaceAll("'", "''") + "'");

					break;
				case 91:
					value = object.optString(colName).replaceAll(
							"T\\d{2}:\\d{2}:\\d{2}", "");

					values.add("'" + value + "'");
					break;
				default:
					value = object.optString(colName);
					values.add((value.equals("")) ? null : value);
				}
			}
		}
		rs.close();
		String sql = "INSERT INTO "
				+ tableName
				+ fields.toString().replaceFirst("^\\[", "(")
						.replaceFirst("\\]$", ")")
				+ " VALUES"
				+ values.toString().replaceFirst("^\\[", "(")
						.replaceFirst("\\]$", ")");

		return sql;
	}

	public static String getSQLUpdate(DatabaseMetaData meta, String tableName,
			JSONObject object) throws SQLException {
		Map stores = new HashMap();
		ResultSet rs = meta.getColumns(null, null, tableName, null);
		while (rs.next()) {
			String colName = rs.getString("COLUMN_NAME");
			if (object.has(colName)) {
				String value;
				switch (rs.getInt("DATA_TYPE")) {
				case 1:
				case 12:
					value = object.optString(colName);
					stores.put(colName, "'" + value.replaceAll("'", "''") + "'");

					break;
				case 91:
					value = object.optString(colName).replaceAll("T[\\d|\\:]+",
							"");

					stores.put(colName, "'" + value + "'");
					break;
				default:
					stores.put(colName, object.opt(colName));
				}
			}
		}
		rs.close();
		String sql = "UPDATE " + tableName + " SET "
				+ stores.toString().replaceAll("^\\{|\\}$", "");

		return sql;
	}
}