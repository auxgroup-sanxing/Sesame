package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import org.jdom.Element;

public class BreakAction extends AbstractAction {
	public boolean isRollbackable() {
		return false;
	}

	public void doinit(Element config) {
	}

	public void dowork(DataContext config) {
		throw new BreakException();
	}
}