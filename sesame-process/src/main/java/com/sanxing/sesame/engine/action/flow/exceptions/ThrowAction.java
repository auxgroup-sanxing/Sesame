package com.sanxing.sesame.engine.action.flow.exceptions;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.exceptions.AppException;
import org.jdom.Element;

public class ThrowAction extends AbstractAction {
	private String exceptionKey;
	private String exceptionMsg;

	public void doinit(Element actionEl) {
		this.exceptionKey = actionEl.getAttributeValue("exception-key");
		this.exceptionMsg = actionEl.getAttributeValue("message");
	}

	public void dowork(DataContext context) throws AppException {
		throw new RuntimeException(this.exceptionKey + "|" + this.exceptionMsg);
	}
}