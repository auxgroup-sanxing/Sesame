package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class TimeTypeHandler implements TypeHandler {
	public Object getField(ResultSet rs, int index) throws SQLException {
		Time value = rs.getTime(index);
		if (rs.wasNull()) {
			value = null;
		}
		return value;
	}

	public void setParameter(PreparedStatement ps, int index, Object value)
			throws SQLException {
		Time v = (Time) value;
		ps.setTime(index, v);
	}
}