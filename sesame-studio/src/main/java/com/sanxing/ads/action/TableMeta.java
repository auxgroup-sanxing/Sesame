package com.sanxing.ads.action;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TableMeta {
	private Connection connection;
	private String tableName;
	private Map fields;
	private String key;
	private boolean has_serial;

	TableMeta() {
		this.fields = new HashMap();

		this.has_serial = false;
	}

	public static TableMeta getTableMeta(Connection conn, String tableName,
			String key) throws SQLException {
		TableMeta instance = new TableMeta();
		instance.connection = conn;
		instance.tableName = tableName;
		instance.key = key;
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getColumns(null, null, tableName, null);
		while (rs.next()) {
			if (rs.getString("TYPE_NAME").toLowerCase().equals("serial")) {
				instance.has_serial = true;
				if (null != TableOperate.getSerial_name()) {
					TableOperate.setSerial_name(rs.getString("COLUMN_NAME"));
				}
			}
			instance.fields.put(rs.getString("COLUMN_NAME"),
					Integer.valueOf(rs.getInt("DATA_TYPE")));
		}

		return instance;
	}

	public boolean hasSerial() {
		return this.has_serial;
	}

	public boolean has(String field) {
		return this.fields.containsKey(field);
	}

	public Connection getConnection() {
		return this.connection;
	}

	public String getTableName() {
		return this.tableName;
	}

	public int getType(String field) {
		return ((Integer) this.fields.get(field)).intValue();
	}

	public List getKeyFields() throws SQLException {
		List list = new ArrayList();
		if ((this.key != null) && (this.key.length() > 0)) {
			for (String eachKey : this.key.split(","))
				list.add(eachKey);
		} else {
			DatabaseMetaData meta = this.connection.getMetaData();
			ResultSet pk = meta.getPrimaryKeys(null, null, this.tableName);
			while (pk.next()) {
				String name = pk.getString("COLUMN_NAME");
				list.add(name);
			}
		}
		return list;
	}
}