package com.sanxing.sesame.jdbc;

import com.sanxing.sesame.jdbc.template.tx.TXRecord;
import com.sanxing.sesame.jdbc.SesameTransactionManager;
import com.sanxing.sesame.util.ServerDetector;
import java.lang.reflect.Method;
import java.util.LinkedList;
import javax.naming.InitialContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

public class TXHelper {
	private static ThreadLocal<LinkedList<TXRecord>> marks = new ThreadLocal();

	static TransactionManager wlsTM = null;

	private static LinkedList<TXRecord> getTXRecords() {
		if (marks.get() == null) {
			marks.set(new LinkedList());
		}
		return ((LinkedList) marks.get());
	}

	private static TransactionManager getFromWebLogicFactory() throws Exception {
		if (wlsTM == null) {
			try {
				Class clazz = Class
						.forName("weblogic.transaction.TransactionHelper");
				Method create = clazz.getMethod("getTransactionHelper",
						new Class[0]);
				Object obj = create.invoke(clazz, null);

				Method m = clazz.getMethod("getTransactionManager",
						new Class[0]);
				wlsTM = (TransactionManager) m.invoke(obj, new Object[0]);
			} catch (ClassNotFoundException e) {
				throw e;
			}
		}

		return wlsTM;
	}

	private static TransactionManager getTransactionManager() {
		TransactionManager tm = null;
		try {
			if (ServerDetector.isWebLogic()) {
				tm = getFromWebLogicFactory();
			} else {
				tm = (TransactionManager) JNDIUtil.getInitialContext().lookup(
						"java:comp/UserTransaction");
			}
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
		return tm;
	}

	public static void beginTX() {
		TransactionManager tm = getTransactionManager();
		TXRecord rec = null;
		try {
			if (tm.getStatus() == 6) {
				tm.begin();
				rec = new TXRecord(1);
			} else {
				rec = new TXRecord(0);
			}
			getTXRecords().add(rec);
		} catch (SystemException e) {
			throw new DataAccessException(e);
		} catch (NotSupportedException nse) {
			throw new DataAccessException(nse);
		}
	}

	public static void beginNewTX() {
		TransactionManager tm = getTransactionManager();
		TXRecord rec = null;
		try {
			if (tm.getStatus() == 6) {
				tm.begin();
				rec = new TXRecord(1);
			} else {
				Transaction tx = tm.suspend();
				tm.begin();
				rec = new TXRecord(2);
				rec.setSuspendedTX(tx);
			}
			getTXRecords().add(rec);
		} catch (SystemException e) {
			throw new DataAccessException(e);
		} catch (NotSupportedException nse) {
			throw new DataAccessException(nse);
		}
	}

	public static void commit() {
		TransactionManager tm = getTransactionManager();
		TXRecord rec = (TXRecord) getTXRecords().getLast();

		if (rec.getType() == 0)
			return;
		try {
			tm.commit();
		} catch (SecurityException se) {
			throw new DataAccessException(se);
		} catch (IllegalStateException ise) {
			throw new DataAccessException(ise);
		} catch (RollbackException re) {
			throw new DataAccessException(re);
		} catch (HeuristicMixedException hme) {
			throw new DataAccessException(hme);
		} catch (HeuristicRollbackException hre) {
			throw new DataAccessException(hre);
		} catch (SystemException e) {
			throw new DataAccessException(e);
		}
	}

	public static void rollback() {
		TransactionManager tm = getTransactionManager();
		TXRecord rec = (TXRecord) getTXRecords().getLast();

		if (rec.getType() != 0)
			try {
				tm.rollback();
			} catch (IllegalStateException ise) {
				throw new DataAccessException(ise);
			} catch (SecurityException se) {
				throw new DataAccessException(se);
			} catch (SystemException e) {
				throw new DataAccessException(e);
			}
		else
			try {
				tm.setRollbackOnly();
			} catch (IllegalStateException ise) {
				throw new DataAccessException(ise);
			} catch (SystemException e) {
				throw new DataAccessException(e);
			}
	}

	public static void close() {
		TransactionManager tm = getTransactionManager();
		TXRecord rec = (TXRecord) getTXRecords().removeLast();
		if (rec.getType() == 2) {
			Transaction tx = rec.getSuspendedTX();
			try {
				tm.resume(tx);
			} catch (InvalidTransactionException ite) {
				throw new DataAccessException(ite);
			} catch (IllegalStateException ise) {
				throw new DataAccessException(ise);
			} catch (SystemException e) {
				throw new DataAccessException(e);
			}
		}
	}

	protected static void destory() {
		SesameTransactionManager.destory();
		if (marks.get() != null) {
			marks.set(null);
			marks.remove();
		}
	}
}