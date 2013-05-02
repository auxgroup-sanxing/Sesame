package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTypeHandler implements TypeHandler {
	public Object getField(ResultSet rs, int index) throws SQLException {
		Long value = null;
		long lValue = rs.getLong(index);
		if (!(rs.wasNull())) {
			value = new Long(lValue);
		}
		return value;
	}

	public void setParameter(PreparedStatement ps, int index, Object value)
			throws SQLException {
		if (value == null) {
			ps.setNull(index, -5);
		} else {
			Long v = (Long) value;
			ps.setLong(index, v.longValue());
		}
	}
}