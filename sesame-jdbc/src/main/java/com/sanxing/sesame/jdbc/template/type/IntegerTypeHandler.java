package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTypeHandler implements TypeHandler {
	public Object getField(ResultSet rs, int index) throws SQLException {
		Integer value = null;
		int iValue = rs.getInt(index);
		if (!(rs.wasNull())) {
			value = new Integer(iValue);
		}
		return value;
	}

	public void setParameter(PreparedStatement ps, int index, Object value)
			throws SQLException {
		if (value == null) {
			ps.setNull(index, 4);
		} else {
			Integer v = (Integer) value;
			ps.setInt(index, v.intValue());
		}
	}
}