package com.sanxing.sesame.engine.action.jdbc;

import com.sanxing.sesame.jdbc.TXHelper;
import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.flow.BreakException;
import com.sanxing.sesame.engine.context.DataContext;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

public class TXAction extends AbstractAction {
	public static final Logger LOG = LoggerFactory.getLogger(TXAction.class);

	public String TX_OPTION_REQUIRED = "required";

	public String TX_OPTION_REQUIRED_NEW = "require-new";
	private Element _config;

	public void doinit(Element config) {
		this._config = config;
	}

	public void dowork(DataContext msgContext) {
		Iterator tryActions = this._config.getChildren().iterator();
		try {
			if (this.TX_OPTION_REQUIRED_NEW.equalsIgnoreCase(this._config
					.getAttributeValue("tx-option"))) {
				TXHelper.beginNewTX();
			} else
				TXHelper.beginTX();
			try {
				ActionUtil.bachInvoke(msgContext, tryActions);
				TXHelper.commit();
			} catch (Throwable t) {
				TXHelper.rollback();
				throw t;
			} finally {
				TXHelper.close();
			}
		} catch (BreakException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable t) {
			throw new ActionException(t);
		}
	}
}