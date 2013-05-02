package com.sanxing.sesame.jdbc.template.type;

import com.sanxing.sesame.jdbc.DataAccessException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TypeHandlerFactory {
	private static Map<Class, TypeHandler> typeHandlerMap = new HashMap();

	static {
		typeHandlerMap.put(String.class, new StringTypeHandler());
		typeHandlerMap.put(Boolean.class, new BooleanTypeHandler());
		typeHandlerMap.put(Short.class, new ShortTypeHandler());
		typeHandlerMap.put(Integer.class, new IntegerTypeHandler());
		typeHandlerMap.put(Long.class, new LongTypeHandler());
		typeHandlerMap.put(BigDecimal.class, new BigDecimalTypeHandler());
		typeHandlerMap.put(Byte[].class, new BytesTypeHandler());
		typeHandlerMap.put(Date.class, new DateTypeHandler());
		typeHandlerMap.put(Time.class, new TimeTypeHandler());
		typeHandlerMap.put(Timestamp.class, new TimestampTypeHandler());
	}

	public static TypeHandler getTypeHandler(Class clazz) {
		TypeHandler typeHandler = (TypeHandler) typeHandlerMap.get(clazz);
		if (typeHandler == null) {
			if (Timestamp.class.isAssignableFrom(clazz))
				typeHandler = (TypeHandler) typeHandlerMap.get(Timestamp.class);
			else if (Date.class.isAssignableFrom(clazz))
				typeHandler = (TypeHandler) typeHandlerMap.get(Date.class);
			else if (Time.class.isAssignableFrom(clazz))
				typeHandler = (TypeHandler) typeHandlerMap.get(Time.class);
			else if (BigDecimal.class.isAssignableFrom(clazz)) {
				typeHandler = (TypeHandler) typeHandlerMap
						.get(BigDecimal.class);
			}
		}
		if (typeHandler == null) {
			throw new DataAccessException("type handler is null");
		}
		return typeHandler;
	}
}