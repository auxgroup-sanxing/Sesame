package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BytesTypeHandler implements TypeHandler {
	public Object getField(ResultSet rs, int index) throws SQLException {
		byte[] value = rs.getBytes(index);
		if (rs.wasNull()) {
			value = (byte[]) null;
		}
		return value;
	}

	public void setParameter(PreparedStatement ps, int index, Object value)
			throws SQLException {
		byte[] v = (byte[]) value;
		ps.setBytes(index, v);
	}
}