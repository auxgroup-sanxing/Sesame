package com.sanxing.studio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.commons.dbcp.BasicDataSource;
import org.json.JSONObject;

public class SQLDataSource {
	public static final String NODECFG = "nodecfg";
	public static final String SUBSERVICE = "subservice";
	public static final String TRANFLDCFG = "tranfldcfg";
	public static final String DIAGRAM = "svcflowcfg";
	public static final String DECISION = "decision";
	public static final String BOOLEXPR = "conditem";
	public static final String SERVICETBL = "service";
	public static final String MSGFLDCFG = "msgfldcfg";
	public static final String TRANSFORMTAR = "subreqcfg";
	public static final String TRANSFORMSRC = "subfldsrccfg";
	public static final String FUNCMAP = "funcmap";
	public static final String TBL_DICT = "msgnameid";
	public static final String TBL_OPTIONS = "optionreg";
	public static final String TBL_USER = "usertbl";
	public static final String TBL_ACCREDIT = "accredit";
	private static BasicDataSource datasource = null;

	public static Connection getConnection() {

		try {
			if (datasource == null) {
				datasource = new BasicDataSource();
				datasource.setDriverClassName(Configuration.getDriverClass());
				datasource.setUrl(Configuration.getConnetionUrl());
				datasource.setUsername(Configuration.getConnectionUser());
				datasource.setPassword(Configuration.getConnectionPasswd());
			}
			
			return datasource.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void closeDataSource() {
		if (datasource == null)
			return;
		try {
			datasource.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		datasource = null;
	}

	public static ResultSet executeQuery(String sqlText) throws SQLException {
		Connection conn = getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			stmt.close();

			return rs;
		} finally {
			conn.close();
		}
	}

	public static int executeDDL(String sqlText) throws SQLException {
		Connection conn = getConnection();
		try {
			Statement stmt = conn.createStatement();
			int result = stmt.executeUpdate(sqlText);
			stmt.close();

			return result;
		} finally {
			conn.close();
		}
	}

	public static int executeDML(String sqlText) throws SQLException {
		Connection conn = getConnection();
		try {
			Statement stmt = conn.createStatement();
			int result = stmt.executeUpdate(sqlText);
			stmt.close();

			return result;
		} finally {
			conn.close();
		}
	}

	public static int getRecordCount(String tableName, String condition)
			throws SQLException {
		Connection conn = getConnection();
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

	public static String getSQLInsert(DatabaseMetaData meta, String tableName,
			JSONObject object) throws SQLException {
		ArrayList fields = new ArrayList();
		ArrayList values = new ArrayList();
		ResultSet rs = meta.getColumns(null, null, tableName, null);
		while (rs.next()) {
			String colName = rs.getString("COLUMN_NAME");
			if (fields.indexOf(colName) <= -1)
				;
			if (object.has(colName)) {
				switch (rs.getInt("DATA_TYPE")) {
				case 1:
				case 12:
					fields.add(colName);
					values.add("'"
							+ object.optString(colName).replaceAll("'", "''")
							+ "'");
					break;
				default:
					fields.add(colName);
					values.add(object.opt(colName));
				}
			}
		}
		rs.close();
		String sql = "INSERT INTO " + tableName
				+ fields.toString().replace('[', '(').replace(']', ')')
				+ " VALUES"
				+ values.toString().replace('[', '(').replace(']', ')');

		return sql;
	}

	public static String getSQLUpdate(DatabaseMetaData meta, String tableName,
			JSONObject object) throws SQLException {
		ArrayList fields = new ArrayList();
		ResultSet rs = meta.getColumns(null, null, tableName, null);
		while (rs.next()) {
			String colName = rs.getString("COLUMN_NAME");
			if (object.has(colName)) {
				switch (rs.getInt("DATA_TYPE")) {
				case 1:
				case 12:
					fields.add(colName + "='"
							+ object.optString(colName).replaceAll("'", "''")
							+ "'");
					break;
				default:
					fields.add(colName + "=" + object.opt(colName));
				}
			}
		}
		rs.close();
		String sql = "UPDATE " + tableName + " SET "
				+ fields.toString().replace("[", "").replace("]", "");
		return sql;
	}
}