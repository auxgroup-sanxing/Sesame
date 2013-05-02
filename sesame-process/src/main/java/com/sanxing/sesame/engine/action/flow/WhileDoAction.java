package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;

public class WhileDoAction extends AbstractAction implements Constant {
	private static final Logger LOG = LoggerFactory.getLogger(WhileDoAction.class);
	private String xpath;
	private Element actionEl;
	private Element contextEl;

	public void doinit(Element actionEl) {
		this.xpath = actionEl.getChildTextTrim("xpath");
		this.actionEl = actionEl;
		this.contextEl = new Element("context");
	}

	public void dowork(DataContext ctx) {
		ExecutionContext executionContext = ctx.getExecutionContext();

		while (getFlag(ctx))
			try {
				Iterator iter = this.actionEl.getChild("do").getChildren()
						.iterator();

				ActionUtil.bachInvoke(ctx, iter);

				if (executionContext.isDebugging()) {
					Element xpathEl = this.actionEl.getChild("xpath");
					synchronized (executionContext) {
						executionContext.setCurrentAction(xpathEl
								.getAttributeValue("id", ""));
						LOG.debug("[Action id="
								+ xpathEl.getAttributeValue("id")
								+ "] I am waiting..." + getName());

						executionContext.wait();
					}
				}
			} catch (BreakException e) {
			} catch (InterruptedException localInterruptedException) {
			}
	}

	private boolean getFlag(DataContext ctx) {
		Variable booleanVar = select(this.contextEl, this.xpath, ctx);
		boolean bool = ((Boolean) booleanVar.get()).booleanValue();
		return bool;
	}

	public void doworkInDehydrateState(DataContext context) {
		Iterator iter = this.actionEl.getChild("do").getChildren().iterator();
		ActionUtil.bachInvoke(context, iter);
	}
}