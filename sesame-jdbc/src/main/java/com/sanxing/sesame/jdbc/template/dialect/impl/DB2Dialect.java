package com.sanxing.sesame.jdbc.template.dialect.impl;

import com.sanxing.sesame.jdbc.data.PageInfo;
import com.sanxing.sesame.jdbc.template.dialect.DataAccessDialect;

public class DB2Dialect implements DataAccessDialect {
	private static DB2Dialect instance = new DB2Dialect();

	public static DB2Dialect getInstance() {
		return instance;
	}

	public String getPagedSql(String sql, int pageNo, int pageSize) {
		PageInfo.vlidatePageInfo(pageNo, pageSize);

		StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
		pagingSelect.append("SELECT * FROM ( SELECT ");
		pagingSelect.append("ROWNUMBER() OVER() AS ROWNUM_,");
		pagingSelect.append(" ROW_.* FROM ( ");
		pagingSelect.append(sql);
		pagingSelect.append(" ) AS ROW_");
		pagingSelect.append(" ) AS TEMP_ WHERE ROWNUM_ ");

		if (pageNo == 1) {
			pagingSelect.append("<= " + String.valueOf(pageSize));
		} else {
			int startPos = (pageNo - 1) * pageSize + 1;
			int endPos = startPos + pageSize - 1;
			pagingSelect.append("BETWEEN " + String.valueOf(startPos) + " AND "
					+ String.valueOf(endPos));
		}

		return pagingSelect.toString();
	}
}