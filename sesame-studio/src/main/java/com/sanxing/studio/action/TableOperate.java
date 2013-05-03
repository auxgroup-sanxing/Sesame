package com.sanxing.studio.action;

import com.sanxing.studio.Authentication;
import com.sanxing.studio.ConnDispenser;
import com.sanxing.studio.utils.Outputter;
import com.sanxing.studio.utils.WebUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TableOperate extends HttpServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(TableOperate.class);
	private static JSONObject currUser = null;
	private static String serial_name = "";
	private static String accredit_userid = "";

	public static String getSerial_name() {
		return serial_name;
	}

	public static void setSerial_name(String serial_name) {
		TableOperate.serial_name = serial_name;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		currUser = Authentication.getCurrentUser();
		String action = request.getParameter("action");
		Connection conn = null;
		try {
			conn = ConnDispenser.getConnection();
			Statement stmt = conn.createStatement();
			if (action != null) {
				if ((action.equals("loadData"))
						|| (action.equals("mulTableLoadData"))) {
					JSONObject result = new JSONObject();
					result.put("success", true);

					String tableName = request.getParameter("table");
					String start = request.getParameter("start");
					String limit = request.getParameter("limit");

					String sql = request.getParameter("sql");

					if ((action.equals("mulTableLoadData"))
							&& (((null == sql) || (sql.length() < 1)))) {
						throw new ServletException("多表操作，必须传入sql语句");
					}

					if (sql != null) {
						sql = new String(sql.getBytes("iso-8859-1"));
						LOG.debug("get sql is:" + sql);
						if (request.getParameter("export") != null) {
							try {
								ResultSet rs = stmt.executeQuery(sql);
								String path = "temp/query_result.xls";
								File file = new File(getServletContext()
										.getRealPath(path));

								Outputter.datasetToExcel(null, rs, file);
								rs.close();
								stmt.close();
								response.sendRedirect(path);
							} catch (Exception e) {
								response.setContentType("text/html; charset=utf-8");
								PrintWriter out = response.getWriter();
								out.print(new Formatter()
										.format("<html><head><script type='text/javascript'>alert('%s');</script></head><body></body></html>",
												new Object[] { e.getMessage() }));
							}

							return;
						}

						if ((start != null) && (limit != null)) {
							if (action.equals("loadData")) {
								result.put("rows",
										loadData(conn, sql, start, limit));
							} else if (action.equals("mulTableLoadData")) {
								JSONArray array = mulTableLoadData(conn, sql,
										start, limit);

								result.put("rows", array.get(0));
								sql = array.getJSONObject(1).getString("sql");
							}

							Pattern p = Pattern.compile("\\sORDER\\s*BY.*", 2);

							Matcher m = p.matcher(sql);
							String countSQL = m.replaceFirst("");
							p = Pattern.compile("(SELECT)\\s.*(FROM\\s.*)", 2);

							m = p.matcher(countSQL);
							result.put(
									"totalCount",
									getRecordCount(conn,
											m.replaceFirst("$1 COUNT(*) $2")));
						} else {
							result.put("rows", loadAll(conn, sql));
						}
					} else {
						String condition = request.getParameter("condition");
						if ((condition != null) && (condition.length() > 0)) {
							condition = new String(
									condition.getBytes("iso-8859-1"), "utf-8");
						}
						LOG.debug("condition is: [" + condition + "]");
						String orderBy = request.getParameter("orderby");
						if ((orderBy != null) && (orderBy.length() > 0)) {
							orderBy = new String(
									orderBy.getBytes("iso-8859-1"), "utf-8");
						}
						LOG.debug("orderby is:[" + orderBy + "]");

						if (request.getParameter("export") != null) {
							sql = "SELECT * FROM " + tableName;
							if ((condition != null) && (condition.length() > 0))
								sql = sql + " WHERE " + condition;
							if ((orderBy != null) && (orderBy.length() > 0))
								sql = sql + " ORDER BY " + orderBy;
							try {
								ResultSet rs = stmt.executeQuery(sql);

								File schemaFolder = new File(
										getServletContext().getRealPath(
												"sqlex/schema"));

								SAXBuilder builder = new SAXBuilder();
								Document document = builder.build(new File(
										schemaFolder, "tables/" + tableName));

								Element tableEl = document.getRootElement();
								String path = "temp/" + tableName + ".xls";
								File file = new File(getServletContext()
										.getRealPath(path));

								Outputter.datasetToExcel(tableEl, rs, file);
								rs.close();
								stmt.close();
								response.sendRedirect(path);
							} catch (Exception e) {
								response.setContentType("text/html; charset=utf-8");
								PrintWriter out = response.getWriter();
								out.print(new Formatter()
										.format("<html><head><script type='text/javascript'>alert('%s');</script></head><body></body></html>",
												new Object[] { e.getMessage() }));
							}

							return;
						}

						if ((start != null) && (limit != null)) {
							result.put(
									"rows",
									loadData(tableName, start, limit,
											condition, orderBy, conn));

							result.put("totalCount",
									getRecordCount(tableName, condition, conn));
						} else {
							result.put(
									"rows",
									loadAll(tableName, condition, orderBy, conn));
						}
					}

					response.setContentType("text/json; charset=utf-8");
					PrintWriter out = response.getWriter();
					out.print(result);
				} else {
					LOG.debug(getServletName() + " GET::" + action);
					Enumeration params = request.getParameterNames();
					while (params.hasMoreElements()) {
						String param = (String) params.nextElement();
						LOG.debug(param + "=" + request.getParameter(param));
					}
				}
			}
			stmt.close();
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			WebUtil.sendError(response, e.getMessage());
		} finally {
			try {
				if ((conn != null) && (!(conn.isClosed())))
					conn.close();
			} catch (SQLException e) {
			}
		}
	}

	public static JSONArray loadAll(String tableName, String condition,
			String orderBy, Connection conn) throws Exception {
		JSONArray result = new JSONArray();
		String sql = "SELECT * FROM " + tableName;
		if ((condition != null) && (condition.length() > 0))
			sql = sql + " WHERE " + condition;
		if ((orderBy != null) && (orderBy.length() > 0))
			sql = sql + " ORDER BY " + orderBy;
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			while (rs.next()) {
				JSONObject record = new JSONObject();
				for (int i = 1; i <= colCount; ++i) {
					if (rs.getObject(i) instanceof String) {
						String value = rightTrim((String) rs.getObject(i));
						record.put(meta.getColumnName(i), value);
					} else {
						record.put(meta.getColumnName(i), rs.getObject(i));
					}
				}

				result.put(record);
			}

			return result;
		} catch (SQLException e) {
			throw e;
		} finally {
			stmt.close();
		}
	}

	public JSONArray loadData(Connection conn, String sql, String start,
			String limit) throws Exception {
		JSONArray result = new JSONArray();
		DatabaseMetaData md = conn.getMetaData();
		Statement stmt = conn.createStatement(1004, 1007);
		try {
			String productName = md.getDatabaseProductName();
			boolean standard = false;
			if (productName.equals("MySQL"))
				sql = sql + " LIMIT " + start + ", " + limit;
			else {
				standard = true;
			}
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();

			if ((standard) && (!(start.equals("0"))))
				rs.absolute(Integer.parseInt(start));
			int count = 0;
			int max = Integer.parseInt(limit);
			while ((rs.next()) && (count < max)) {
				JSONObject record = new JSONObject();
				for (int i = 1; i <= colCount; ++i) {
					if (rs.getObject(i) instanceof String) {
						String value = rightTrim((String) rs.getObject(i));
						record.put(meta.getColumnName(i), value);
					} else {
						record.put(meta.getColumnName(i), rs.getObject(i));
					}
				}
				result.put(record);
				++count;
			}

			rs.close();

			return result;
		} catch (SQLException e) {
			throw e;
		} finally {
			stmt.close();
		}
	}

	public JSONArray mulTableLoadData(Connection conn, String sql,
			String start, String limit) throws Exception {
		JSONArray result = new JSONArray();
		DatabaseMetaData md = conn.getMetaData();
		Statement stmt = conn.createStatement(1004, 1007);

		String fields = sql.split("@")[0];
		String tableNames = sql.split("@")[1];
		String condition = sql.split("@")[2];

		JSONArray array = new JSONArray(fields);
		fields = "";
		for (int i = 0; i < array.length(); ++i) {
			fields = fields + array.getString(i) + ",";
		}
		System.out.println("fields is:" + fields);

		List columnNumList = new ArrayList();
		List tableNameList = new ArrayList();

		String fieldsSQL = "";
		String tableNamesSQL = "";

		for (String tname : tableNames.split(",")) {
			condition = condition.replaceAll(tname + "_", tname + ".");
		}

		for (int y = 0; y < tableNames.split(",").length; ++y) {
			String tableName = tableNames.split(",")[y];
			int i = 0;
			for (String fieldName : fields.split(",")) {
				if (fieldName.startsWith(tableName + "_")) {
					fieldsSQL = fieldsSQL
							+ fieldName.replaceAll(
									new StringBuilder().append("^")
											.append(tableName).append("_")
											.toString(), new StringBuilder()
											.append(tableName).append(".")
											.toString()) + ",";

					++i;
				}
			}
			columnNumList.add(Integer.valueOf(i));
			tableNameList.add(tableName);
			tableNamesSQL = tableNamesSQL + tableName + ",";
		}
		sql = " SELECT " + fieldsSQL.substring(0, fieldsSQL.length() - 1)
				+ " FROM "
				+ tableNamesSQL.substring(0, tableNamesSQL.length() - 1)
				+ " WHERE " + "" + condition;

		LOG.debug("mul table load data sql is:" + sql);
		try {
			JSONObject jsonSQL = new JSONObject();
			jsonSQL.put("sql", sql);
			String productName = md.getDatabaseProductName();
			boolean standard = false;
			if (productName.equals("MySQL"))
				sql = sql + " LIMIT " + start + ", " + limit;
			else {
				standard = true;
			}
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();

			if ((standard) && (!(start.equals("0"))))
				rs.absolute(Integer.parseInt(start));
			int count = 0;
			int max = Integer.parseInt(limit);
			while ((rs.next()) && (count < max)) {
				JSONObject record = new JSONObject();

				Iterator numIt = columnNumList.iterator();
				Iterator tableNameIt = tableNameList.iterator();

				String tableName = (String) tableNameIt.next();
				int FieldNum = ((Integer) numIt.next()).intValue();
				for (int i = 1; i <= colCount; ++i) {
					if (i > FieldNum) {
						tableName = (String) tableNameIt.next();
						int key = ((Integer) numIt.next()).intValue();
						FieldNum += key;
					}
					if (rs.getObject(i) instanceof String) {
						String value = rightTrim((String) rs.getObject(i));
						record.put(tableName + "_" + meta.getColumnName(i),
								value);
					} else {
						record.put(tableName + "_" + meta.getColumnName(i),
								rs.getObject(i));
					}

				}

				result.put(record);
				++count;
			}

			rs.close();
			JSONArray resultContainSQL = new JSONArray();
			resultContainSQL.put(result);

			resultContainSQL.put(jsonSQL);
			LOG.debug("mulTableLoadData retrun result is:"
					+ resultContainSQL.toString());

			return result;
		} catch (Exception e) {
			e.printStackTrace();

			throw e;
		} finally {
			stmt.close();
		}
	}

	public JSONArray loadAll(Connection conn, String sql) throws Exception {
		JSONArray result = new JSONArray();
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			while (rs.next()) {
				JSONObject record = new JSONObject();
				for (int i = 1; i <= colCount; ++i) {
					if (rs.getObject(i) instanceof String) {
						String value = rightTrim((String) rs.getObject(i));
						record.put(meta.getColumnName(i), value);
					} else {
						record.put(meta.getColumnName(i), rs.getObject(i));
					}
				}
				result.put(record);
			}

			return result;
		} catch (SQLException e) {
			throw e;
		} finally {
			conn.close();
		}
	}

	private JSONArray loadData(String tableName, String start, String limit,
			String condition, String orderBy, Connection conn) throws Exception {
		JSONArray result = new JSONArray();
		String sql = "SELECT * FROM " + tableName;
		DatabaseMetaData md = conn.getMetaData();
		Statement stmt = conn.createStatement(1004, 1007);
		try {
			String productName = md.getDatabaseProductName();
			if ((condition != null) && (condition.length() > 0))
				sql = sql + " WHERE " + condition;
			if ((orderBy != null) && (orderBy.length() > 0)) {
				sql = sql + " ORDER BY " + orderBy;
			}
			boolean standard = false;
			if (productName.equals("MySQL"))
				sql = sql + " LIMIT " + start + ", " + limit;
			else {
				standard = true;
			}

			LOG.debug("loadData sql is:[" + sql + "]");
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();

			if ((standard) && (!(start.equals("0"))))
				rs.absolute(Integer.parseInt(start));
			int count = 0;
			int max = Integer.parseInt(limit);
			while ((rs.next()) && (count < max)) {
				JSONObject record = new JSONObject();
				for (int i = 1; i <= colCount; ++i) {
					record.put(meta.getColumnName(i), rs.getObject(i));
				}
				result.put(record);
				++count;
			}

			rs.close();

			return result;
		} finally {
			stmt.close();
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		currUser = Authentication.getCurrentUser();
		accredit_userid = request.getParameter("accredit_userid");
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		String key = request.getParameter("key");
		LOG.debug("key is:" + key);

		response.setContentType("text/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		Connection conn = null;
		try {
			conn = ConnDispenser.getConnection();
			if (action != null) {
				if (action.equals("mulTableSaveData")) {
					try {
						JSONObject result = new JSONObject();
						JSONObject _result = new JSONObject();
						String tableName = request.getParameter("table");

						String[] tables = tableName.split(",");
						String data = request.getParameter("data");
						JSONArray records = new JSONArray(data);

						String all = records.toString();
						if (records.length() < 1) {
							result.put("success", true);
							result.put("affected", 0);
						} else {
							JSONObject record = records.getJSONObject(0);
							String operation = record.getString("~");

							Map table_records = new HashMap();
							for (String eachTableName : tables) {
								JSONArray eachRecords = new JSONArray();
								JSONObject eachRecord = new JSONObject();
								JSONObject eachModified = new JSONObject();

								eachRecord.put("~", operation);

								if (operation.equals("update")) {
									JSONObject modified = record
											.getJSONObject("@");
									Iterator keys = modified.keys();
									while (keys.hasNext()) {
										String name = (String) keys.next();
										Object value = null;
										if (name.startsWith(eachTableName + "_")) {
											value = modified.get(name);
											name = name.replace(eachTableName
													+ "_", "");

											eachModified.put(name, value);
										}
									}

									if (eachModified.length() < 1)
										continue;
									eachRecord.put("@", eachModified);
								}

								Iterator keys = record.keys();
								while (keys.hasNext()) {
									String name = (String) keys.next();
									Object value = null;

									if (key != null) {
										for (String tName : tables) {
											if (name.equals(tName + "_" + key)) {
												eachRecord.put(key,
														record.get(name));
											}
										}
									}
									if (name.startsWith(eachTableName + "_")) {
										value = record.get(name);
										name = name.replace(
												eachTableName + "_", "");

										eachRecord.put(name, value);
									}

								}

								eachRecords.put(eachRecord);
								table_records.put(eachTableName, eachRecords);
							}
							try {
								conn.setAutoCommit(false);
								int columnMaxValue = -1;
								JSONArray rec = new JSONArray();
								for (String eachTableName : tables) {
									if (null == table_records
											.get(eachTableName))
										continue;
									if (((JSONArray) table_records
											.get(eachTableName)).length() < 1) {
										continue;
									}

									if ((operation.equals("insert"))
											&& (columnMaxValue > 0)) {
										((JSONArray) table_records
												.get(eachTableName))
												.getJSONObject(0).put(key,
														columnMaxValue);
									}

									LOG.debug("each record is:"
											+ ((JSONArray) table_records
													.get(eachTableName))
													.toString());

									_result = saveData(eachTableName, key,
											(JSONArray) table_records
													.get(eachTableName), conn);

									System.out.println("result is:"
											+ _result.toString());

									if ((!(operation.equals("insert")))
											|| (columnMaxValue >= 0))
										continue;
									rec = _result.getJSONArray("generated");
									columnMaxValue = rec.getInt(0);
									if (columnMaxValue < 0) {
										throw new Exception(
												"can not get the max value of "
														+ key);
									}

								}

								conn.commit();

								_result.put("generated", rec);
								result = _result;
							} catch (Exception e) {
								throw e;
							} finally {
								conn.setAutoCommit(true);
							}
						}
						LOG.debug("result is:" + result.toString());

						out.print(result);
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (action.equals("execSQL")) {
					JSONObject result = new JSONObject();
					result.put("success", true);
					String sql = request.getParameter("sql");
					sql = sql.trim();
					if (sql.length() == 0)
						WebUtil.sendError(response, "不能执行空语句");
					if (sql.substring(0, 6).toUpperCase().equals("SELECT")) {
						JSONArray columns = execQuery(conn, sql);
						result.put("columns", columns);
					} else {
						int affected = ConnDispenser.executeDML(conn, sql);
						result.put("affected", affected);
					}
					out.print(result);
				} else if (action.equals("saveData")) {
					String tableName = request.getParameter("table");

					String data = request.getParameter("data");
					JSONArray records = new JSONArray(data);
					conn.setAutoCommit(false);
					JSONObject result = new JSONObject();
					try {
						result = saveData(tableName, key, records, conn);
						conn.commit();
					} catch (Exception e) {
						throw e;
					} finally {
						conn.setAutoCommit(true);
					}
					LOG.debug("data is:" + result);
					out.print(result);
				} else {
					LOG.debug(getServletName() + " POST::" + action);
					Enumeration params = request.getParameterNames();
					while (params.hasMoreElements()) {
						String param = (String) params.nextElement();
						LOG.debug(param + "=" + request.getParameter(param));
					}
				}
			}
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			WebUtil.sendError(response, e.getMessage());
		} finally {
			try {
				if ((conn != null) && (!(conn.isClosed())))
					conn.close();
			} catch (SQLException e) {
			}
		}
	}

	private JSONArray execQuery(Connection conn, String sql)
			throws SQLException, JSONException {
		JSONArray result = new JSONArray();
		Statement stmt = conn.createStatement();
		try {
			stmt.setFetchSize(1);
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int i = 1;
			for (int count = meta.getColumnCount(); i <= count; ++i) {
				JSONObject column = new JSONObject();
				column.put("header", meta.getColumnLabel(i));
				column.put("dataIndex", meta.getColumnName(i));
				String type = meta.getColumnTypeName(i).toLowerCase();
				if (type.indexOf("char") > -1) {
					type = "string";
				} else if (type.indexOf("int") > -1) {
					type = "int";
				} else if (type.equals("date")) {
					column.put("dateFormat", "Y-m-d");
				} else if (type.indexOf("decimal") > -1) {
					type = "float";
				} else if (type.equals("time")) {
					type = "date";
					column.put("dateFormat", "H:i:s");
				} else if (type.equals("timestamp")) {
					type = "string";
				} else if (type.equals("serial")) {
					type = "string";
				} else {
					type = "string";
				}
				column.put("type", type);
				result.put(column);
			}
			rs.close();
		} finally {
			stmt.close();
		}
		return result;
	}

	private int getRecordCount(Connection conn, String countSQL)
			throws SQLException {
		Statement stmt = conn.createStatement(1004, 1007);
		try {
			ResultSet rs = stmt.executeQuery(countSQL);
			int result = 0;
			int rows = 1;
			if (rs.next())
				result = rs.getInt(1);
			while (rs.next())
				++rows;
			rs.close();
			if (rows > 1)
				result = rows;
			int i = result;

			return i;
		} finally {
			stmt.close();
		}
	}

	private int getRecordCount(String tableName, String condition,
			Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT COUNT(*) FROM "
							+ tableName
							+ (((condition != null) && (condition.length() > 0)) ? " WHERE "
									+ condition
									: ""));

			int result = 0;
			if (rs.next())
				result = rs.getInt(1);
			rs.close();
			int i = result;

			return i;
		} finally {
			stmt.close();
		}
	}

	public static JSONObject saveData(String tableName, String key,
			JSONArray records, Connection conn) throws Exception {
		JSONObject result = new JSONObject();
		int affected = 0;
		TableMeta meta = TableMeta.getTableMeta(conn, tableName, key);
		try {
			for (int i = 0; i < records.length(); ++i) {
				JSONObject record = records.getJSONObject(i);
				String all = record.toString();
				String action = record.getString("~");
				record.remove("~");
				if (action != null) {
					if (action.equals("insert")) {
						if (tableName.equals("ty_ywcsb")) {
							if (null == currUser) {
								throw new Exception(
										" can not get the current user");
							}
							String teller = currUser.getString("userid");

							String org = currUser.getString("organ");
							record.put("visa_teller", teller);
							record.put("visa_date", getCurrentDate());
							record.put("open_date", getCurrentDate());
						} else if (tableName.equals("ty_sfdwkz")) {
							record.put("chk_no", "001");
							record.put("run_flag", "0");
							record.put("work_date", getCurrentDate());
						}

						JSONObject ro = execInsert(meta, tableName, record);
						affected += ro.getInt("affected");

						result.append("generated", ro.get("generated"));
					} else if (action.equals("update")) {
						JSONObject ro = execUpdate(meta, tableName, record);
						affected += ro.getInt("affected");
					} else if (action.equals("delete")) {
						JSONObject ro = execDelete(meta, tableName, record);
						affected += ro.getInt("affected");
					}
				}
				if ((null == accredit_userid)
						|| (accredit_userid.length() <= 0) || (affected <= 0))
					continue;
				saveAccreditLog(meta.getConnection(), action, tableName, all);
			}
		} catch (Exception e) {
			LOG.debug(e.getMessage());
			throw e;
		}

		result.put("success", true);
		result.put("affected", affected);
		return result;
	}

	public static JSONObject execDelete(TableMeta meta, String tableName,
			JSONObject object) throws SQLException, JSONException {
		ArrayList exprs = new ArrayList();
		ArrayList values = new ArrayList();
		List list = meta.getKeyFields();
		Iterator iter;
		Iterator keys;
		if (list.size() > 0)
			for (iter = list.iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				Object value = object.get(name);
				if (value == null) {
					exprs.add(name + " IS NULL");
				} else {
					if (value instanceof String) {
						if (rightTrim((String) value).length() < 1) {
							exprs.add("(" + name + " IS NULL OR " + name
									+ " ='')");
						}

						value = ((String) value).replaceAll(
								"^(\\d{4}-\\d{2}-\\d{2})T(00:00:00)$", "$1");
					}

					exprs.add(name + "=?");
					values.add(value);
				}
			}
		else {
			for (keys = object.keys(); keys.hasNext();) {
				String colName = (String) keys.next();
				if (meta.has(colName)) {
					Object value = object.get(colName);
					int type = meta.getType(colName);
					if (value == null)
						exprs.add(colName + " IS NULL");
					else if (value.equals("")) {
						if ((type != 1) && (type != 12))
							exprs.add(colName + " IS NULL");
						else {
							exprs.add("(" + colName + " IS NULL OR " + colName
									+ "='')");
						}
					} else {
						switch (type) {
						case 1:
						case 12:
							if (rightTrim((String) value).length() < 1) {
								exprs.add("(" + colName + " IS NULL OR "
										+ colName + " ='')");
							} else {
								exprs.add(colName + "=?");
								values.add(value);
							}
							break;
						case 91:
							value = ((String) value)
									.replaceAll(
											"^(\\d{4}-\\d{2}-\\d{2})T(00:00:00)$",
											"$1");

							exprs.add(colName + "=?");
							values.add(value);
							break;
						case 4:
							exprs.add(colName + "=?");
							values.add(Integer.valueOf(object.optInt(colName)));
							break;
						default:
							exprs.add(colName + "=?");
							values.add(value);
						}
					}
				}
			}
		}

		String sql = "DELETE FROM "
				+ tableName
				+ " WHERE "
				+ exprs.toString().replaceAll("^\\[|\\]$", "")
						.replaceAll(",", " AND");

		LOG.debug("sql is:" + sql);
		JSONObject result = new JSONObject();
		try {
			PreparedStatement stmt = meta.getConnection().prepareStatement(sql);
			int i = 0;
			for (int len = values.size(); i < len; ++i) {
				if (values.get(i) instanceof String) {
					LOG.debug("set value is:[" + values.get(i) + "]");
					stmt.setString(i + 1, (String) values.get(i));
				} else {
					stmt.setObject(i + 1, values.get(i));
					LOG.debug("set value is:[" + values.get(i) + "]");
				}
			}
			int affected = stmt.executeUpdate();
			result.put("affected", affected);
		} catch (SQLException e) {
			LOG.debug(sql);
			throw e;
		}
		return result;
	}

	public static JSONObject execInsert(TableMeta meta, String tableName,
			JSONObject object) throws SQLException, JSONException {
		ArrayList fields = new ArrayList();
		ArrayList values = new ArrayList();

		for (Iterator keys = object.keys(); keys.hasNext();) {
			String colName = (String) keys.next();
			if (fields.indexOf(colName) > -1) {
				continue;
			}
			if (meta.has(colName)) {
				fields.add(colName);
				String value;
				switch (meta.getType(colName)) {
				case 1:
				case 12:
					value = object.optString(colName);
					values.add((value.equals("null")) ? null : value);
					break;
				case 91:
					value = object.optString(colName).replaceAll(
							"T\\d{2}:\\d{2}:\\d{2}", "");

					values.add(value);
					break;
				default:
					values.add(object.opt(colName));
				}
			}
		}
		String sql = "INSERT INTO "
				+ tableName
				+ fields.toString().replaceFirst("^\\[", "(")
						.replaceFirst("\\]$", ")");

		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (int len = fields.size(); i < len; ++i) {
			buffer.append(",?");
		}
		sql = sql + buffer.toString().replaceFirst("\\,", " VALUES(") + ")";
		LOG.debug("sql is:" + sql);
		JSONObject result = new JSONObject();
		try {
			PreparedStatement stmt = meta.getConnection().prepareStatement(sql);
			i = 0;
			for (int len = fields.size(); i < len; ++i) {
				LOG.debug("set values is:[" + values.get(i) + "]");
				stmt.setObject(i + 1, values.get(i));
			}
			int affected = stmt.executeUpdate();
			result.put("affected", affected);

			if (meta.hasSerial()) {
				int columnMaxValue = ConnDispenser.getColumnMaxValue(tableName,
						serial_name, meta.getConnection());

				result.put("generated", columnMaxValue);
			} else {
				result.put("generated", "");
			}
		} catch (SQLException e) {
			LOG.debug(sql);
			throw e;
		}
		return result;
	}

	public static JSONObject execUpdate(TableMeta meta, String tableName,
			JSONObject object) throws SQLException, JSONException {
		JSONObject modified = object.getJSONObject("@");
		for (Iterator keys = modified.keys(); keys.hasNext();) {
			String name = (String) keys.next();
			Object value = object.get(name);
			object.put(name, modified.get(name));
			modified.put(name, value);
		}
		if (tableName.equals("ty_ywcsb")) {
			String teller = currUser.getString("userid");
			String org = currUser.getString("organ");
			modified.put("mod_teller", teller);
			modified.put("mod_org", org);
			modified.put("mod_date", getCurrentDate());
		}
		object.remove("@");

		ArrayList fields = new ArrayList();
		ArrayList values = new ArrayList();
		for (Iterator keys = modified.keys(); keys.hasNext();) {
			String colName = (String) keys.next();
			if (meta.has(colName)) {
				Object value = modified.get(colName);
				int type = meta.getType(colName);
				if ((value == null)
						|| ((type != 1) && (type != 12) && (value.equals("")))) {
					fields.add(colName + "=null");
				}

				fields.add(colName + "=?");
				switch (type) {
				case 1:
				case 12:
					value = modified.optString(colName);
					values.add(value);
					break;
				case 91:
					value = modified.optString(colName).replaceAll(
							"^(\\d{4}-\\d{2}-\\d{2})T(00:00:00)$", "$1");

					values.add(value);
					break;
				case 4:
					values.add(Integer.valueOf(modified.optInt(colName)));
					break;
				case 6:
					values.add(Double.valueOf(modified.optDouble(colName)));
					break;
				default:
					values.add(modified.get(colName));
				}
			}
		}
		String sql = "UPDATE " + tableName + " SET "
				+ fields.toString().replaceAll("^\\[|\\]$", "");

		ArrayList exprs = new ArrayList();
		List list = meta.getKeyFields();
		Iterator iter;
		Iterator keys;
		if (list.size() > 0)
			for (iter = list.iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				Object value = object.opt(name);
				if (value == null) {
					exprs.add(name + " IS NULL");
				} else {
					if (value instanceof String) {
						value = ((String) value).replaceAll(
								"^(\\d{4}-\\d{2}-\\d{2})T(00:00:00)$", "$1");
					}

					exprs.add(name + "=?");
					values.add(value);
				}
			}
		else {
			for (keys = object.keys(); keys.hasNext();) {
				String colName = (String) keys.next();
				if (meta.has(colName)) {
					Object value = object.get(colName);
					int type = meta.getType(colName);
					if (value == null)
						exprs.add(colName + " IS NULL");
					else if (value.equals("")) {
						if ((type != 1) && (type != 12))
							exprs.add(colName + " IS NULL");
						else {
							exprs.add("(" + colName + " IS NULL OR " + colName
									+ "='')");
						}
					} else {
						switch (type) {
						case 1:
						case 12:
							exprs.add(colName + "=?");
							values.add(object.getString(colName));
							break;
						case 91:
							value = object
									.getString(colName)
									.replaceAll(
											"^(\\d{4}-\\d{2}-\\d{2})T(00:00:00)$",
											"$1");

							exprs.add(colName + "=?");
							values.add(value);
							break;
						case 4:
							exprs.add(colName + "=?");
							values.add(Integer.valueOf(object.optInt(colName)));
							break;
						default:
							exprs.add(colName + "=?");
							values.add(value);
						}
					}
				}
			}
		}

		sql = sql
				+ " WHERE "
				+ exprs.toString().replaceAll("^\\[|\\]$", "")
						.replaceAll(",", " AND");

		LOG.debug("sql is:" + sql);
		JSONObject result = new JSONObject();
		try {
			PreparedStatement stmt = meta.getConnection().prepareStatement(sql);
			int i = 0;
			for (int len = values.size(); i < len; ++i) {
				if (values.get(i) instanceof String) {
					LOG.debug("set value is:[" + values.get(i) + "]");
					stmt.setString(i + 1, (String) values.get(i));
				} else {
					stmt.setObject(i + 1, values.get(i));
					LOG.debug("set value is:[" + values.get(i) + "]");
				}
			}
			int affected = stmt.executeUpdate();
			result.put("affected", affected);
		} catch (SQLException e) {
			LOG.debug(sql);
			throw e;
		}
		return result;
	}

	public static String getCurrentDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(new Date());
	}

	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}

	public static String rightTrim(String str) {
		int spaceNum = 0;
		for (int i = str.length() - 1; (i >= 0) && (str.charAt(i) == ' '); --i) {
			++spaceNum;
		}

		return str.substring(0, str.length() - spaceNum);
	}

	public static void saveAccreditLog(Connection conn, String operation,
			String tableName, String data) throws Exception {
		PreparedStatement ps = conn
				.prepareStatement("insert into op_log(current_operator,accredit_operator,operation,op_time,table_name,record)values(?,?,?,?,?,?)");
		try {
			System.out.println("user id:is:" + accredit_userid);
			ps.setString(1, currUser.getString("userid"));
			ps.setString(2, accredit_userid);
			ps.setString(3, operation);
			ps.setObject(4, getCurrentTime());
			ps.setString(5, tableName);
			ps.setString(6, data);

			ps.execute();
		} catch (Exception e) {
		} finally {
			ps.close();
		}
	}
}