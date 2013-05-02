package com.sanxing.sesame.jdbc.template;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract interface IndexedRowMapping {
	public abstract Object handleRow(ResultSet paramResultSet, int paramInt)
			throws SQLException;
}