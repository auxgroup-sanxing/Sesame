package com.sanxing.studio.action;

import com.sanxing.studio.ConnDispenser;
import com.sanxing.studio.utils.WebUtil;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class TableBrowser extends HttpServlet implements Servlet {
	private static final long serialVersionUID = 10000L;
	private static final Logger log = LoggerFactory.getLogger(TableBrowser.class);

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		String data = request.getParameter("data");
		if (data != null) {
			data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
		}
		response.setContentType("text/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		Connection conn = null;
		try {
			conn = ConnDispenser.getConnection();
			Statement stmt = conn.createStatement();
			if (action != null)
				if (action.equals("getDBInfo")) {
					response.setContentType("text/plain; charset=utf-8");
					DatabaseMetaData meta = conn.getMetaData();
					String info = "数据库类型: " + meta.getDatabaseProductName()
							+ meta.getDatabaseProductVersion() + "\n";

					out.write(info);
				} else {
					System.out.println(action + "->" + data);
				}
			stmt.close();
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			WebUtil.sendError(response, e.getMessage());
		} finally {
			try {
				if ((conn != null) && (!(conn.isClosed())))
					conn.close();
			} catch (SQLException e) {
			}
		}
	}

	public JSONArray loadData(String tableName, Statement stmt)
			throws Exception {
		JSONArray result = new JSONArray();
		String sqlText = "SELECT * FROM " + tableName;
		try {
			ResultSet rs = stmt.executeQuery(sqlText);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			while (rs.next()) {
				JSONObject record = new JSONObject();
				for (int i = 1; i <= colCount; ++i) {
					record.put(meta.getColumnName(i), rs.getObject(i));
				}
				result.put(record);
			}
		} finally {
			stmt.close();
		}
		return result;
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");

		response.setContentType("text/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		Connection conn = null;
		try {
			conn = ConnDispenser.getConnection();
			if (action != null)
				if (action.equals("loadData")) {
					String tableName = request.getParameter("table");
					Statement stmt = conn.createStatement();
					JSONArray records = loadData(tableName, stmt);
					JSONObject result = new JSONObject();
					result.put("rows", records);
					out.print(result);
					stmt.close();
				} else if (action.equals("saveData")) {
					String tableName = request.getParameter("table");
					JSONArray records = new JSONArray(
							request.getParameter("data"));
					saveData(tableName, records, conn);
					out.print(true);
				} else {
					System.out.println(getServletName() + " POST::" + action);
					Enumeration params = request.getParameterNames();
					while (params.hasMoreElements()) {
						String param = (String) params.nextElement();
						System.out.println(param + "="
								+ request.getParameter(param));
					}
				}
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			WebUtil.sendError(response, e.getMessage());
		} finally {
			try {
				if ((conn != null) && (!(conn.isClosed())))
					conn.close();
			} catch (SQLException e) {
			}
		}
	}

	private void saveData(String tableName, JSONArray records, Connection conn)
			throws Exception {
		DatabaseMetaData meta = conn.getMetaData();
		try {
			ArrayList columns = new ArrayList();
			ResultSet rs = meta.getColumns(null, null, tableName, null);
			while (rs.next())
				columns.add(rs.getString("COLUMN_NAME"));
			rs.close();
			for (int i = 0; i < records.length(); ++i) {
				JSONObject record = records.getJSONObject(i);
				String action = record.getString("~");
				record.remove("~");
				if (action != null)
					if (action.equals("insert")) {
						String sql = ConnDispenser.getSQLInsert(meta,
								tableName, record);

						ConnDispenser.executeDDL(conn, sql);
					} else if (action.equals("update")) {
						JSONObject modified = record.getJSONObject("@");
						Iterator keys = modified.keys();
						while (keys.hasNext()) {
							String key = (String) keys.next();
							Object value = record.get(key);
							record.put(key, modified.get(key));
							modified.put(key, value);
						}
						String sql = ConnDispenser.getSQLUpdate(meta,
								tableName, modified);

						record.remove("@");
						ArrayList exprs = new ArrayList();
						for (Iterator iter = columns.iterator(); iter.hasNext();) {
							String key = (String) iter.next();
							if (!(record.has(key)))
								break;
							String value = record.getString(key);
							if (value.equals("")) {
								exprs.add("(" + key + " IS NULL OR " + key
										+ "='')");
							} else {
								if (record.get(key) instanceof String)
									value = "'" + value.replaceAll("'", "''")
											+ "'";
								exprs.add(key + "=" + value);
							}
						}
						sql = sql
								+ " WHERE "
								+ exprs.toString().replace('[', ' ')
										.replace(']', ' ')
										.replaceAll(",", " AND");

						ConnDispenser.executeDDL(conn, sql);
					} else if (action.equals("delete")) {
						String sql = "DELETE FROM " + tableName;
						ArrayList exprs = new ArrayList();
						for (Iterator iter = columns.iterator(); iter.hasNext();) {
							String key = (String) iter.next();
							if (!(record.has(key)))
								break;
							String value = record.getString(key);
							if (value.equals("")) {
								exprs.add("(" + key + " IS NULL OR " + key
										+ "='')");
							} else {
								if (record.get(key) instanceof String)
									value = "'" + value.replaceAll("'", "''")
											+ "'";
								exprs.add(key + "=" + value);
							}
						}
						sql = sql
								+ " WHERE "
								+ exprs.toString().replaceFirst("^\\[", "")
										.replaceFirst("\\]$", "")
										.replaceAll(",", " AND ");

						ConnDispenser.executeDDL(conn, sql);
					}
			}
		} finally {
		}
	}
}