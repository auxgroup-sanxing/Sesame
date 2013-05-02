package com.sanxing.sesame.jdbc.template.impl;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.DataAccessUtil;
import com.sanxing.sesame.jdbc.template.IndexedRowMapping;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import com.sanxing.sesame.jdbc.template.dialect.DataAccessDialect;
import com.sanxing.sesame.jdbc.template.dialect.DataAccessDialectManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTemplateImpl {
	private static final Logger LOG = LoggerFactory.getLogger(BaseTemplateImpl.class);

	private String dsName = null;

	BaseTemplateImpl(String dsName) {
		this.dsName = dsName;
	}

	protected List exeQuery(String sql, ParametersMapping paramMapping,
			IndexedRowMapping indexedRowMapping) {
		logSql(sql);
		List retList = new ArrayList();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		DataSource ds = TemplateHelper.getDataSource(this.dsName);
		try {
			conn = ds.getConnection();
			ps = conn.prepareStatement(sql);
			if (paramMapping != null) {
				paramMapping.setParameters(ps);
			}
			rs = ps.executeQuery();
			int rowNo = 0;
			while (rs.next()) {
				++rowNo;
				Object row = indexedRowMapping.handleRow(rs, rowNo);
				retList.add(row);
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} finally {
			DataAccessUtil.closeResultSet(rs);
			DataAccessUtil.closeStatement(ps);
			DataAccessUtil.closeConnection(conn);
		}
		return retList;
	}

	protected void validateSize(int len) {
		if (len > 1) {
			String exMsg = "multi-row:" + String.valueOf(len);
			throw new DataAccessException(exMsg);
		}
	}

	protected int exeUpdate(String sql, ParametersMapping paramMapping) {
		logSql(sql);
		int rowNo = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		DataSource ds = TemplateHelper.getDataSource(this.dsName);
		try {
			conn = ds.getConnection();
			ps = conn.prepareStatement(sql);
			if (paramMapping != null) {
				paramMapping.setParameters(ps);
			}
			rowNo = ps.executeUpdate();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} finally {
			DataAccessUtil.closeStatement(ps);
			DataAccessUtil.closeConnection(conn);
		}
		return rowNo;
	}

	protected DataAccessDialect getDataAccessDialect() {
		return DataAccessDialectManager.getDataAccessDialect();
	}

	private void logSql(String sql) {
		if (LOG.isDebugEnabled()) {
			String logStr = "SQL:" + sql;
			LOG.debug(logStr);
		}
	}
}