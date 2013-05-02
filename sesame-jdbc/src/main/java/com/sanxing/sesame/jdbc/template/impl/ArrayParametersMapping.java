package com.sanxing.sesame.jdbc.template.impl;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArrayParametersMapping implements ParametersMapping {
	private Object[] parameters = null;
	private int[] paramTypes = null;

	public ArrayParametersMapping(Object[] parameters) {
		this.parameters = parameters;
	}

	public ArrayParametersMapping(Object[] parameters, int[] paramTypes) {
		this.parameters = parameters;
		this.paramTypes = paramTypes;
	}

	public void setParameters(PreparedStatement ps) throws SQLException {
		if (this.parameters != null) {
			int len = this.parameters.length;
			if (len > 0)
				for (int i = 0; i < len; ++i) {
					int parameterIndex = i + 1;
					try {
						if (this.paramTypes == null) {
							ps.setObject(parameterIndex, this.parameters[i]);
						} else {
							ps.setObject(parameterIndex, this.parameters[i],
									this.paramTypes[i]);
						}
					} catch (Exception e) {
						throw new DataAccessException("parameter["
								+ String.valueOf(parameterIndex) + "] error:"
								+ e.getMessage(), e);
					}
				}
		}
	}
}