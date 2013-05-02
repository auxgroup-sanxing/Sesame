package com.sanxing.sesame.jdbc.template.impl;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import com.sanxing.sesame.jdbc.template.type.TypeHandler;
import com.sanxing.sesame.jdbc.template.type.TypeHandlerFactory;
import com.sanxing.sesame.util.BeanUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BeanParametersMapping implements ParametersMapping {
	private Object parameters = null;

	private String[] paramNames = null;

	public BeanParametersMapping(Object parameters, String[] paramNames) {
		this.parameters = parameters;
		this.paramNames = paramNames;
	}

	public void setParameters(PreparedStatement ps) throws SQLException {
		int paramLen = this.paramNames.length;
		if (paramLen > 0)
			for (int i = 0; i < paramLen; ++i) {
				String paramName = this.paramNames[i];
				try {
					int parameterIndex = i + 1;
					Class paramClazz = BeanUtil.getPropertyType(
							this.parameters, paramName);

					if (paramClazz == null) {
						throw new DataAccessException("paramName :["
								+ paramName + "] not in paramameters ["
								+ this.parameters + "]");
					}

					TypeHandler typeHandler = TypeHandlerFactory
							.getTypeHandler(paramClazz);
					Object paramValue = BeanUtil.getProperty(this.parameters,
							paramName);
					typeHandler.setParameter(ps, parameterIndex, paramValue);
				} catch (Exception e) {
					throw new DataAccessException("parameter[" + paramName
							+ "] error:" + e.getMessage(), e);
				}
			}
	}
}