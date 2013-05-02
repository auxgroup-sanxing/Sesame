package com.sanxing.sesame.logging.util;

import com.sanxing.sesame.logging.dao.SesameBaseDAO;
import com.sanxing.sesame.logging.dao.DAOFactory;
import com.sanxing.sesame.logging.dao.LogBean;

public class DAOHelper {
	public static LogBean query(SesameBaseDAO dao, LogBean log) {
		return dao.queryForRecord(log);
	}

	public static SesameBaseDAO getDAO(LogBean log, boolean callout) {
		DAOFactory factory = DAOFactory.getDaoFactoryInstance();
		SesameBaseDAO dao = factory.productDAO(log, callout);
		return dao;
	}

	public static boolean updateOnDuplicate(SesameBaseDAO dao, LogBean log) {
		return dao.updateOnDuplicate(log);
	}

	public static boolean updateStateOnDuplicate(SesameBaseDAO dao,
			LogBean log) {
		return dao.updateStateOnDuplicate(log);
	}

	public static void insert(SesameBaseDAO dao, LogBean log) {
		dao.insert(log);
	}
}