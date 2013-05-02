package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.Iterator;
import java.util.List;
import org.jdom2.Element;

public class DecisionAction extends AbstractAction {
	private Element actionEl;
	private Element contextEl;

	public void doinit(Element actionEl) {
		this.actionEl = actionEl;
		this.contextEl = new Element("context");
	}

	public void dowork(DataContext ctx) {
		boolean exit = false;

		List list = this.actionEl.getChildren("if");
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Element ifEl = (Element) iter.next();

			String xpath = ifEl.getChildTextTrim("xpath");
			Variable booleanVar = select(this.contextEl, xpath, ctx);
			Boolean bool = (Boolean) booleanVar.get();
			Element thenEl = ifEl.getChild("then");
			if (bool.booleanValue()) {
				Iterator actions = thenEl.getChildren().iterator();
				ActionUtil.bachInvoke(ctx, actions);
				exit = true;
				break;
			}
		}

		if ((!(exit)) && (this.actionEl.getChild("default") != null)) {
			Iterator actions = this.actionEl.getChild("default").getChildren()
					.iterator();
			ActionUtil.bachInvoke(ctx, actions);
		}
	}

	public void doworkInDehydrateState(DataContext context) {
		boolean exit = false;

		List list = this.actionEl.getChildren("if");

		for (int i = 0; i < list.size(); ++i) {
			Element ifEl = (Element) list.get(i);
			Element thenEl = ifEl.getChild("then");
			Iterator actions = thenEl.getChildren().iterator();
			ActionUtil.bachInvoke(context, actions);
		}

		if (this.actionEl.getChild("default") != null) {
			Iterator actions = this.actionEl.getChild("default").getChildren()
					.iterator();
			ActionUtil.bachInvoke(context, actions);
		}
	}
}