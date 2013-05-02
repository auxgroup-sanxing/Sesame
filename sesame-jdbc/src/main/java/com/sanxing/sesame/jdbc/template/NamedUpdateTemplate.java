package com.sanxing.sesame.jdbc.template;

import java.util.Map;

public abstract interface NamedUpdateTemplate {
	public abstract int update(String paramString,
			Map<String, Object> paramMap, Map<String, Integer> paramMap1);

	public abstract int update(String paramString, Map<String, Object> paramMap);

	public abstract int update(String paramString, Object paramObject);
}