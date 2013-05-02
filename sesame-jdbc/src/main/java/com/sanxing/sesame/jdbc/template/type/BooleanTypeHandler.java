package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTypeHandler implements TypeHandler {
	public Object getField(ResultSet rs, int index) throws SQLException {
		Boolean value = null;
		boolean bValue = rs.getBoolean(index);
		if (!(rs.wasNull())) {
			if (bValue)
				value = Boolean.TRUE;
			else {
				value = Boolean.FALSE;
			}
		}
		return value;
	}

	public void setParameter(PreparedStatement ps, int index, Object value)
			throws SQLException {
		Boolean v = (Boolean) value;
		ps.setBoolean(index, v.booleanValue());
	}
}