package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

public class ForEachAction extends AbstractAction implements Constant {
	String varName;
	String xpath;
	String childVarName;
	Element config;

	public void doinit(Element config) {
		this.varName = config.getAttributeValue("var");

		this.xpath = config.getChildTextTrim("xpath");

		this.childVarName = config.getAttributeValue("as");

		this.config = config;
	}

	public String toString() {
		return "ForEachAction{varName='" + this.varName + '\'' + ", xpath='"
				+ this.xpath + '\'' + ", childVarName='" + this.childVarName
				+ '\'' + '}';
	}

	public void dowork(DataContext ctx) {
		Variable var = ctx.getVariable(this.varName);
		Element ele = (Element) var.get();
		Variable listVar = null;
		try {
			listVar = select(ele, this.xpath, ctx);
		} catch (Exception e) {
			ActionException ae = new ActionException(this, e);
			ae.setErrorCode("00011");
			throw ae;
		}
		if (listVar.get() instanceof Element) {
			Element child = (Element) listVar.get();
			Variable childVar = new Variable(child, 0);
			ctx.addVariable(this.childVarName, childVar);
			Iterator iterActions = this.config.getChild("actions")
					.getChildren().iterator();
			try {
				ActionUtil.bachInvoke(ctx, iterActions);
			} catch (BreakException localBreakException1) {
			}
		} else {
			List elements = (List) listVar.get();

			Iterator eleIter = elements.iterator();
			while (eleIter.hasNext()) {
				Element child = (Element) eleIter.next();
				Variable childVar = new Variable(child, 0);
				ctx.addVariable(this.childVarName, childVar);
				Iterator iterActions = this.config.getChild("actions")
						.getChildren().iterator();
				try {
					ActionUtil.bachInvoke(ctx, iterActions);
				} catch (BreakException e) {
					return;
				}
			}
		}
	}

	public void doworkInDehydrateState(DataContext ctx) {
		Iterator iterActions = this.config.getChild("actions").getChildren()
				.iterator();
		try {
			ActionUtil.bachInvoke(ctx, iterActions);
		} catch (BreakException localBreakException) {
		}
	}
}