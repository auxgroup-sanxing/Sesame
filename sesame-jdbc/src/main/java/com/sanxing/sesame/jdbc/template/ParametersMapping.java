package com.sanxing.sesame.jdbc.template;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract interface ParametersMapping {
	public abstract void setParameters(PreparedStatement paramPreparedStatement)
			throws SQLException;
}