package com.sanxing.sesame.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameTransaction implements Transaction {
	private static Logger LOG = LoggerFactory.getLogger(SesameTransaction.class);

	private int status = 0;

	private boolean isSuspend = false;

	private int idx = 0;

	private SesameConnection jdbcCon = null;

	private static ThreadLocal<LinkedList<SesameTransaction>> transactions = new ThreadLocal();

	static ThreadLocal<LinkedList<SesameTransaction>> getTransactions() {
		return transactions;
	}

	public Connection getConnection() {
		return this.jdbcCon;
	}

	public void bindConnection(Connection con) {
		this.jdbcCon = ((SesameConnection) con);
	}

	public void suspend() throws SQLException {
		this.isSuspend = true;
	}

	public boolean isSuspend() {
		return this.isSuspend;
	}

	public int getIdx() {
		return this.idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public void resume() {
		this.isSuspend = false;
	}

	private void reallyCloseConn() {
		try {
			this.jdbcCon.setAutoCommit(true);
			this.jdbcCon.reallyClose();
		} catch (Throwable t) {
			try {
				LOG.error(t.getMessage(), t);
			} catch (Throwable localThrowable1) {
			}
		}
	}

	public void commit() throws HeuristicMixedException,
			HeuristicRollbackException, RollbackException, SecurityException,
			SystemException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("TX [" + this + "] commit....");
		}

		if (this.isSuspend) {
			throw new SystemException("tx is suspended, resume it first");
		}

		if (this.status == 1) {
			rollback();
			if (LOG.isTraceEnabled()) {
				LOG.trace("TX [" + this + "] rollback");
			}
			throw new RollbackException("transaction is rollbacked");
		}
		try {
			this.status = 8;
			this.jdbcCon.commit();
			if (LOG.isTraceEnabled()) {
				LOG.trace("TX [" + this + "] commited");
			}
			this.status = 3;
			reallyCloseConn();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new SystemException(e.getMessage());
		}
		((LinkedList) transactions.get()).removeLast();
	}

	public void rollback() throws IllegalStateException, SystemException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("TX [" + this + "] rollback....");
		}

		if (canRollback()) {
			if (this.isSuspend)
				throw new IllegalStateException(
						"tx is suspended, resume it first");
			try {
				this.status = 9;
				this.jdbcCon.rollback();
				if (LOG.isTraceEnabled()) {
					LOG.trace("TX [" + this + "] rollbacked....");
				}
				this.status = 4;
				reallyCloseConn();
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
				throw new SystemException(e.getMessage());
			} finally {
				((LinkedList) transactions.get()).removeLast();
			}
		}
	}

	boolean canRollback() {
		return ((this.status != 3) && (this.status != 9) && (this.status != 4));
	}

	public boolean delistResource(XAResource xaRes, int flag)
			throws IllegalStateException, SystemException {
		return false;
	}

	public boolean enlistResource(XAResource xaRes)
			throws IllegalStateException, RollbackException, SystemException {
		return false;
	}

	public int getStatus() throws SystemException {
		return this.status;
	}

	public void registerSynchronization(Synchronization synch)
			throws IllegalStateException, RollbackException, SystemException {
	}

	public void setRollbackOnly() throws IllegalStateException, SystemException {
		this.status = 1;
	}

	public String toString() {
		return "sesameTransaction [status=" + this.status + ", isSuspend="
				+ this.isSuspend + ", idx=" + this.idx + ", jdbcCon="
				+ this.jdbcCon + "]";
	}
}