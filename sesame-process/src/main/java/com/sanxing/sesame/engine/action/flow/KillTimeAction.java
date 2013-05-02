package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import org.jdom2.Element;

public class KillTimeAction extends AbstractAction {
	long waitTime = 0L;

	public void doinit(Element config) {
		this.waitTime = Integer.parseInt(config.getAttributeValue("wait"));
	}

	public void dowork(DataContext config) {
		try {
			Thread.sleep(this.waitTime);
		} catch (InterruptedException localInterruptedException) {
		}
	}
}