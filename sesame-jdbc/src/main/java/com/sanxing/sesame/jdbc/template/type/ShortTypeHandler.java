package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShortTypeHandler implements TypeHandler {
	public Object getField(ResultSet rs, int index) throws SQLException {
		Short value = null;
		short shortValue = rs.getShort(index);
		if (!(rs.wasNull())) {
			value = new Short(shortValue);
		}
		return value;
	}

	public void setParameter(PreparedStatement ps, int index, Object value)
			throws SQLException {
		if (value == null) {
			ps.setNull(index, 5);
		} else {
			Short v = (Short) value;
			ps.setShort(index, v.shortValue());
		}
	}
}