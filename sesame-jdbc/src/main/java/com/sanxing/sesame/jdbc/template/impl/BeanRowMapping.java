package com.sanxing.sesame.jdbc.template.impl;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.RowMapping;
import com.sanxing.sesame.jdbc.template.type.TypeHandler;
import com.sanxing.sesame.jdbc.template.type.TypeHandlerFactory;
import com.sanxing.sesame.util.BeanUtil;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class BeanRowMapping implements RowMapping {
	private Class rowClazz = null;

	public BeanRowMapping(Class rowClazz) {
		this.rowClazz = rowClazz;
	}

	public Object handleRow(ResultSet rs) throws SQLException {
		Object row = null;
		try {
			row = this.rowClazz.newInstance();
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int count = rsMetaData.getColumnCount();
			if (count > 0)
				for (int i = 1; i <= count; ++i) {
					String colName = rsMetaData.getColumnName(i);
					try {
						Class colClazz = BeanUtil.getPropertyType(row, colName);
						if (colClazz != null) {
							TypeHandler typeHandler = TypeHandlerFactory
									.getTypeHandler(colClazz);
							Object colValue = typeHandler.getField(rs, i);
							BeanUtil.setProperty(row, colName, colValue);
						}
					} catch (Exception e) {
						throw new DataAccessException("column[" + colName
								+ "] error:" + e.getMessage(), e);
					}
				}
		} catch (InstantiationException ise) {
			throw new DataAccessException(ise);
		} catch (IllegalAccessException iae) {
			throw new DataAccessException(iae);
		}
		return row;
	}
}