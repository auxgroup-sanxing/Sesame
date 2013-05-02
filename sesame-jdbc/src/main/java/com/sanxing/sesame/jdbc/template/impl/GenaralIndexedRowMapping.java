package com.sanxing.sesame.jdbc.template.impl;

import com.sanxing.sesame.jdbc.template.IndexedRowMapping;
import com.sanxing.sesame.jdbc.template.RowMapping;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenaralIndexedRowMapping implements IndexedRowMapping {
	private RowMapping rowMapping = null;

	public GenaralIndexedRowMapping(RowMapping rowMapping) {
		this.rowMapping = rowMapping;
	}

	public Object handleRow(ResultSet rs, int rowNo) throws SQLException {
		return this.rowMapping.handleRow(rs);
	}
}