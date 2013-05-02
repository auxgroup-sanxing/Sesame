package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import org.jdom2.Element;

public class FinishAction extends AbstractAction {
	public boolean isRollbackable() {
		return false;
	}

	public void doinit(Element config) {
	}

	public void dowork(DataContext context) {
		try {
			context.getExecutionContext().closeDebugging();
			context.getExecutionContext().setCurrentAction("exit");
		} catch (InterruptedException localInterruptedException) {
		}
		throw new AbortException();
	}
}