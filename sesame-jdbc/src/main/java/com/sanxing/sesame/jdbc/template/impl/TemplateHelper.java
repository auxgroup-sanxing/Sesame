package com.sanxing.sesame.jdbc.template.impl;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.JNDIUtil;
import com.sanxing.sesame.jdbc.template.DataAccessTemplate;
import com.sanxing.sesame.jdbc.template.tx.TXTemplate;
import com.sanxing.sesame.jdbc.template.tx.impl.TXTemplateImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class TemplateHelper {
	private static String PARAMETER_MARK = "#";

	public static TXTemplate getTXTemplate(int txType) {
		return new TXTemplateImpl(txType);
	}

	public static DataAccessTemplate getDataAccessTemplate(String dsName) {
		return new DataAccessTemplateImpl(dsName);
	}

	static DataSource getDataSource(String dsName) {
		DataSource ds = null;
		try {
			ds = (DataSource) JNDIUtil.getInitialContext().lookup(dsName);
		} catch (NamingException e) {
			throw new DataAccessException(e);
		}
		if (ds == null) {
			throw new DataAccessException("data source[" + dsName + "] is null");
		}
		return ds;
	}

	public static InputInfo getInputInfo(String sql) {
		List paramNameList = new ArrayList();
		StringTokenizer sqlParser = new StringTokenizer(sql, PARAMETER_MARK,
				true);
		StringBuilder realSqlBuilder = new StringBuilder(1);
		String token = null;
		String lastToken = null;

		while (sqlParser.hasMoreTokens()) {
			token = sqlParser.nextToken();
			if (PARAMETER_MARK.equals(lastToken)) {
				if (PARAMETER_MARK.equals(token)) {
					realSqlBuilder.append(PARAMETER_MARK);
					token = null;
				} else {
					paramNameList.add(token);
					realSqlBuilder.append("?");
					token = sqlParser.nextToken();
					if (!(PARAMETER_MARK.equals(token))) {
						throw new DataAccessException(
								"parameter mark is invalid");
					}
					token = null;
				}
			} else if (!(PARAMETER_MARK.equals(token))) {
				realSqlBuilder.append(token);
			}

			lastToken = token;
		}

		String realSql = realSqlBuilder.toString();
		int paramLen = paramNameList.size();
		String[] paramName = new String[paramLen];
		for (int i = 0; i < paramLen; ++i) {
			paramName[i] = ((String) paramNameList.get(i));
		}
		InputInfo iInfo = new InputInfo(realSql, paramName);
		return iInfo;
	}
}