package com.sanxing.ads.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransformWriter {
	private int index = 1;
	private int parentId = -1;
	private PreparedStatement stmtSource;
	private PreparedStatement stmtTarget;
	private Connection connection;

	public TransformWriter(int svcid, int subseq, Connection conn)
			throws SQLException {
		this.connection = conn;
		this.stmtTarget = conn
				.prepareStatement("INSERT INTO subreqcfg VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		this.stmtSource = conn
				.prepareStatement("INSERT INTO subfldsrccfg VALUES(?, ?, ?, ?, ?, ?, ?, ?)");

		this.stmtTarget.setInt(1, svcid);
		this.stmtTarget.setInt(2, subseq);
	}

	public void closeStmt() {
		try {
			this.stmtTarget.close();
			this.stmtSource.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveMappings(JSONArray mappings, int level, int repeat,
			int repSubseq, int repMsgid, int repFldid, int kind, int flag)
			throws SQLException, JSONException {
		for (int i = 0; i < mappings.length(); ++i) {
			JSONObject field = mappings.getJSONObject(i);
			JSONArray innerFields = field.optJSONArray("fields");
			if (innerFields != null) {
				int index = this.index;
				JSONObject countField = field.optJSONObject("countField");
				int lRepeat = (countField == null) ? 0 : 1;
				int lSubseq = (countField == null) ? 0 : countField
						.optInt("subseq");
				int lMsgid = (countField == null) ? 0 : countField
						.optInt("msgid");
				int lFldid = (countField == null) ? 0 : countField
						.optInt("fldid");
				int lLevel = level;
				if (!(field.optBoolean("isHeader", false))) {
					index = this.index++;
					lLevel = level + 1;
					this.stmtTarget.setInt(3, this.parentId--);
					this.stmtTarget.setInt(4, index);
					this.stmtTarget.setInt(5, lSubseq);
					this.stmtTarget.setInt(6, lMsgid);
					this.stmtTarget.setInt(7, lFldid);
					this.stmtTarget.setInt(8, 0);
					this.stmtTarget.setInt(9, getNewMapId());
					this.stmtTarget.setInt(10, flag);
					this.stmtTarget.setInt(11, kind);
					this.stmtTarget.setInt(12, lRepeat);
					this.stmtTarget.setInt(13, lLevel);
					this.stmtTarget.executeUpdate();
				}
				saveMappings(innerFields, lLevel, lRepeat, lSubseq, lMsgid,
						lFldid, kind, flag);
			} else {
				int mapId = getNewMapId();
				this.stmtTarget.setInt(3, field.getInt("fldid"));
				this.stmtTarget.setInt(4, this.index++);
				this.stmtTarget.setInt(5, repSubseq);
				this.stmtTarget.setInt(6, repMsgid);
				this.stmtTarget.setInt(7, repFldid);
				this.stmtTarget.setInt(8, field.optInt("funcId"));
				this.stmtTarget.setInt(9, mapId);
				this.stmtTarget.setInt(10, flag);
				this.stmtTarget.setInt(11, kind);
				this.stmtTarget.setInt(12, repeat);
				this.stmtTarget.setInt(13, level);
				this.stmtTarget.executeUpdate();
				saveParams(field.getJSONArray("params"), mapId);
			}
		}
	}

	private void saveParams(JSONArray array, int mapId) throws JSONException,
			SQLException {
		int seq = 0;
		for (int i = 0; i < array.length(); ++i) {
			JSONObject param = array.getJSONObject(i);
			this.stmtSource.setInt(1, mapId);
			this.stmtSource.setInt(2, seq++);
			this.stmtSource.setInt(3, param.optInt("msgid"));
			this.stmtSource.setInt(4, param.optInt("subseq"));
			this.stmtSource.setInt(5, param.optInt("fldid"));
			this.stmtSource.setInt(6, param.optInt("selStart"));
			this.stmtSource.setInt(7, param.optInt("selEnd"));
			this.stmtSource.setString(8, param.optString("value"));
			this.stmtSource.executeUpdate();
		}
	}

	private int getNewMapId() throws SQLException {
		Statement stmt = this.connection.createStatement();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT MAX(asmid) FROM subreqcfg");
			rs.next();
			int i = rs.getInt(1) + 1;

			return i;
		} finally {
			stmt.close();
		}
	}

	public void resetIndex() {
		this.index = 1;
		this.parentId = -1;
	}
}