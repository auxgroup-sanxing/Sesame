package com.sanxing.sesame.runtime;

import com.sanxing.sesame.core.naming.JNDIUtil;
import com.sanxing.sesame.serial.SerialGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBSNGenerator extends SerialGenerator {
	private AtomicLong limit = new AtomicLong();
	private DataSource ds;
	private static String DATASOURCE_NAME = "sesame-datasource";

	private static String QUERY_SERIAL_SQL = " SELECT CUR_SERIAL FROM SESAME_SYSCTL FOR UPDATE ";
	private static String UPDATE_SERIAL_SQL = " UPDATE SESAME_SYSCTL SET CUR_SERIAL = ? ";

	public DBSNGenerator() throws NamingException {
		InitialContext namingContext = JNDIUtil.getInitialContext();
		this.ds = ((DataSource) namingContext.lookup(DATASOURCE_NAME));
		if (this.ds == null)
			throw new RuntimeException("DataSource not found: ["
					+ DATASOURCE_NAME + "]");
	}

	public long getLimit() {
		return this.limit.get();
	}

	public long allocate() {
		try {
			Connection conn = this.ds.getConnection();
			try {
				long sn = 1L;
				conn.setAutoCommit(false);
				PreparedStatement selectStmt = conn
						.prepareStatement(QUERY_SERIAL_SQL);
				PreparedStatement updateStmt = conn
						.prepareStatement(UPDATE_SERIAL_SQL);

				ResultSet rs = selectStmt.executeQuery();
				if (rs.next()) {
					sn = rs.getLong(1);
					updateStmt.setLong(1, sn + 1000L);
					updateStmt.execute();
				} else {
					throw new Exception(
							"In the table [SESAME_SYSCTL] , CUR_SERIAL is not initialize!");
				}
				rs.close();
				conn.commit();

				this.limit.set(sn + 1000L);
				return sn;
			} catch (SQLException e) {
				throw e;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}