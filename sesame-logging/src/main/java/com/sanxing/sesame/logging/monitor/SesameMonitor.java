package com.sanxing.sesame.logging.monitor;

import com.sanxing.sesame.jdbc.data.PageInfo;
import com.sanxing.sesame.logging.dao.SesameBaseDAO;
import com.sanxing.sesame.logging.dao.DAOFactory;
import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.dao.LogQueryBean;
import java.io.PrintStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameMonitor implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(SesameMonitor.class);

	public List<LogBean> queryForRecordSet(SQLCondition condition,
			PageInfo pageInfo, boolean callout) {
		LogQueryBean bean = new LogQueryBean(condition);
		DAOFactory daoFactory = DAOFactory.getDaoFactoryInstance();
		SesameBaseDAO dao = daoFactory.productDAO(bean, callout);
		return (List<LogBean>) dao.queryForRecordSet(bean, pageInfo);
	}

	public LogBean queryForRecord(SQLCondition condition, boolean callout) {
		LogQueryBean bean = new LogQueryBean(condition);
		DAOFactory daoFactory = DAOFactory.getDaoFactoryInstance();
		SesameBaseDAO dao = daoFactory.productDAO(bean, callout);
		return dao.queryForRecord(bean);
	}

	public long queryCount(SQLCondition condition, boolean callout) {
		LogQueryBean bean = new LogQueryBean(condition);
		DAOFactory daoFactory = DAOFactory.getDaoFactoryInstance();
		SesameBaseDAO dao = daoFactory.productDAO(bean, callout);
		return dao.queryCount(bean);
	}

	public LogBean getMessage(long timeout) {
		StoppableMessageQueue queue = StoppableMessageQueue.getInstance();
		LogBean data = (LogBean) queue.getMessage(timeout);
		return data;
	}

	public static void main(String[] args) {
		SesameMonitor monitor = new SesameMonitor();
		SQLCondition condition = new SQLCondition();
		condition.add("serialNumber", "=", "11");
		LogBean data = monitor.queryForRecord(condition, false);
		System.out.println(data);
	}

	public void run() {
		while (true) {
			LOG.debug("start getMessage");
			getMessage(0L);
			LOG.debug("after getMessage");
		}
	}
}