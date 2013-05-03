package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.context.DataContext;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

public class GroupAction extends AbstractAction {
	Element config;

	public void doinit(Element config) {
		this.config = config;
	}

	public void dowork(DataContext ctx) {
		Iterator actions = this.config.getChildren().iterator();
		ActionUtil.bachInvoke(ctx, actions);
	}
}