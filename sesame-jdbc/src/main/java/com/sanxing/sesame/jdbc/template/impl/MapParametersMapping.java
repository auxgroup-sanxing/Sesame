package com.sanxing.sesame.jdbc.template.impl;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class MapParametersMapping implements ParametersMapping {
	private Map<String, Object> parameters = null;

	private String[] paramNames = null;

	private Map<String, Integer> paramTypes = null;

	public MapParametersMapping(Map<String, Object> parameters,
			String[] paramNames) {
		this.parameters = parameters;
		this.paramNames = paramNames;
	}

	public MapParametersMapping(Map<String, Object> parameters,
			String[] paramNames, Map<String, Integer> paramTypes) {
		this.parameters = parameters;
		this.paramNames = paramNames;
		this.paramTypes = paramTypes;
	}

	public void setParameters(PreparedStatement ps) throws SQLException {
		if (this.parameters != null) {
			int paramLen = this.paramNames.length;
			if (paramLen > 0)
				for (int i = 0; i < paramLen; ++i) {
					String paramName = this.paramNames[i];
					try {
						int parameterIndex = i + 1;
						Object paramValue = this.parameters.get(paramName);
						if (this.paramTypes == null) {
							ps.setObject(parameterIndex, paramValue);
						} else {
							int paramType = ((Integer) this.paramTypes
									.get(paramName)).intValue();
							ps.setObject(parameterIndex, paramValue, paramType);
						}
					} catch (Exception e) {
						throw new DataAccessException("parameter[" + paramName
								+ "] error:" + e.getMessage(), e);
					}
				}
		}
	}
}