package com.sanxing.sesame.logging.handlers;

import com.sanxing.sesame.jdbc.TXHelper;
import com.sanxing.sesame.logging.dao.SesameBaseDAO;
import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.util.DAOHelper;

public class DataBaseHandler implements LogHandler {
	public void handle(LogBean bean) {
		TXHelper.beginTX();
		try {
			SesameBaseDAO calloutDao = DAOHelper.getDAO(bean, true);
			DAOHelper.insert(calloutDao, bean);
			if (!(bean.isCallout())) {
				SesameBaseDAO dao = DAOHelper.getDAO(bean, false);
				DAOHelper.insert(dao, bean);
			}
			TXHelper.commit();
		} catch (Exception e) {
			try {
				TXHelper.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			TXHelper.close();
		}
	}
}