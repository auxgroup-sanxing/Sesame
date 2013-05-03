package com.sanxing.sesame.engine.action.var;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import org.jdom.Element;

public class CloneAction extends AbstractAction implements Constant {
	String targetVarName;
	String xPath;
	String sourceVarName;

	public void doinit(Element config) {
		this.sourceVarName = config.getAttributeValue("var");
		this.targetVarName = config.getAttributeValue("to-var");
		this.xPath = config.getChildTextTrim("xpath");
	}

	public void dowork(DataContext ctx) {
		Variable var = getVariable(ctx, this.sourceVarName, this.xPath);
		Variable target = (Variable) var.clone();
		ctx.addVariable(this.targetVarName, target);
	}
}