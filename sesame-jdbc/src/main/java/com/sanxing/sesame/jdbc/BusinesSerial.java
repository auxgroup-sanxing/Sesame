package com.sanxing.sesame.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinesSerial {
	private int period = 1000;

	private static final Logger LOG = LoggerFactory.getLogger(BusinesSerial.class);

	private AtomicBoolean persist = new AtomicBoolean(true);

	private AtomicLong serial = new AtomicLong(0L);

	private AtomicLong preSerial = new AtomicLong(0L);
	private String today;
	static Map<String, BusinesSerial> serials = new HashMap();

	public void setPersist(boolean _persist) {
		this.persist.set(_persist);
	}

	private long getCurrentSerialFromDB(String day) {
		Connection con = null;
		Statement state = null;
		ResultSet set = null;
		long serial = 0L;
		try {
			DataSource ds = (DataSource) JNDIUtil.getInitialContext().lookup(
					"STM_DATASOURCE");
			con = ds.getConnection();
			state = con.createStatement();
			set = state
					.executeQuery("select BSERIAL  from B_SERIAL where BDAY = '"
							+ day + "'");

			while (set.next())
				serial = set.getLong(1);
		} catch (Exception e) {
			throw new RuntimeException("db err", e);
		} finally {
			DataAccessUtil.closeResultSet(set);
			DataAccessUtil.closeStatement(state);
			DataAccessUtil.closeConnection(con);
		}
		return serial;
	}

	private BusinesSerial(String _today) {
		this.today = _today;
		this.serial.set(getCurrentSerialFromDB(_today));
		this.preSerial.set(this.serial.get());
		LOG.debug("current serial" + this.preSerial);
		resetCusor();
	}

	public String nextSerial() {
		long temp = this.serial.getAndIncrement();
		if (temp == this.preSerial.get()) {
			resetCusor();
		}
		String noPad = "" + temp;
		return this.today + StringUtils.leftPad(noPad, 12, "0");
	}

	private void resetCusor() {
		this.preSerial.getAndAdd(this.period);
		LOG.debug("persits preSerial" + this.persist.get());
		if (this.persist.get())
			persist();
	}

	public void persist() {
		Connection con = null;
		Statement state = null;
		try {
			DataSource ds = (DataSource) JNDIUtil.getInitialContext().lookup(
					"STM_DATASOURCE");
			con = ds.getConnection();
			con.setAutoCommit(true);
			state = con.createStatement();
			state.executeUpdate(" update B_SERIAL set BSERIAL = "
					+ this.preSerial.get() + " where BDAY = '" + this.today
					+ "'");
		} catch (Exception e) {
			throw new RuntimeException("db err", e);
		} finally {
			DataAccessUtil.closeStatement(state);
			DataAccessUtil.closeConnection(con);
		}
	}

	public static BusinesSerial getInstance(String day) {
		if (!(serials.containsKey(day))) {
			BusinesSerial serial = new BusinesSerial(day);
			serials.put(day, serial);
		}
		return ((BusinesSerial) serials.get(day));
	}

	public static void main(String[] args) {
	}
}