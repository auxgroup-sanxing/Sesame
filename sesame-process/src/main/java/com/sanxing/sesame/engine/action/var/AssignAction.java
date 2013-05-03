package com.sanxing.sesame.engine.action.var;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.context.VariableFactory;
import org.jdom.Element;

public class AssignAction extends AbstractAction implements Constant {
	String targetVarName;
	String xPath;
	String sourceVarName;
	String rawText;
	String targetVarType;
	String rawValue;
	private boolean cloneObject;

	public void doinit(Element config) {
		this.targetVarName = config.getAttributeValue("to-var");

		this.sourceVarName = config.getAttributeValue("var");

		this.cloneObject = config.getAttributeValue("clone", "false").equals(
				"true");

		this.targetVarType = config.getAttributeValue("type");

		this.xPath = config.getChildTextTrim("xpath");

		this.rawValue = config.getChildTextTrim("raw-value");
	}

	public void dowork(DataContext ctx) {
		Variable targetVar;
		if (this.sourceVarName == null) {
			targetVar = VariableFactory.getIntance(this.rawValue,
					this.targetVarType);
		} else {
			Variable srcVar = ctx.getVariable(this.sourceVarName);
			if ((srcVar.getVarType() != 0) && (this.xPath != null)
					&& (this.xPath.length() > 0)) {
				ActionException ae = new ActionException(this, "00003");
				ae.setErrMsgArgs(new String[] { this.sourceVarName });
			}
			targetVar = getVariable(ctx, this.sourceVarName, this.xPath);
		}
		if (this.cloneObject) {
			targetVar = (Variable) targetVar.clone();
		}

		ctx.addVariable(this.targetVarName, targetVar);
	}
}