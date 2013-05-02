package com.sanxing.sesame.jdbc.template.dialect;

public abstract interface DataAccessDialect {
	public abstract String getPagedSql(String paramString, int paramInt1,
			int paramInt2);
}