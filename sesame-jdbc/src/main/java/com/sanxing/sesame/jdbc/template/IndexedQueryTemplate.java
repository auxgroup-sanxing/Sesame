package com.sanxing.sesame.jdbc.template;

import java.util.List;
import java.util.Map;

public abstract interface IndexedQueryTemplate {
	public abstract Map<String, Object> queryRow(String paramString,
			Object[] paramArrayOfObject, int[] paramArrayOfInt);

	public abstract List<Map<String, Object>> query(String paramString,
			Object[] paramArrayOfObject, int[] paramArrayOfInt);

	public abstract List<Map<String, Object>> query(String paramString,
			Object[] paramArrayOfObject, int[] paramArrayOfInt, int paramInt1,
			int paramInt2);

	public abstract Map<String, Object> queryRow(String paramString,
			Object[] paramArrayOfObject);

	public abstract List<Map<String, Object>> query(String paramString,
			Object[] paramArrayOfObject);

	public abstract List<Map<String, Object>> query(String paramString,
			Object[] paramArrayOfObject, int paramInt1, int paramInt2);
}