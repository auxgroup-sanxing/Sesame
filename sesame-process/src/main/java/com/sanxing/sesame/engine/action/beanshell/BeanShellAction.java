package com.sanxing.sesame.engine.action.beanshell;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.exceptions.AppException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;
import org.jdom.Text;

public class BeanShellAction extends AbstractAction {
	Logger LOG = LoggerFactory.getLogger(BeanShellAction.class);
	private String script;

	public void doinit(Element config) {
		this.script = config.getText();
	}

	public void dowork(DataContext context) throws AppException {
		BeanShellContext bsc = (BeanShellContext) context.getExecutionContext()
				.get("beanshell.context");
		String functionName = "func_" + getActionId();
		if (!(bsc.isFunctionResitered(functionName))) {
			String temp = functionName + "(){" + this.script + "}";

			bsc.registerFunction(functionName, temp);
		}
		Map vars = context.getVariables();
		Iterator keys = vars.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Variable var = (Variable) vars.get(key);
			if (this.LOG.isTraceEnabled()) {
				this.LOG.trace("set var [" + var + "] to shell context");
			}
			Object value = var.get();
			if (value instanceof Text) {
				value = ((Text) value).getTextTrim();
			}
			bsc.set(key, value);
		}
		bsc.set("ctx", context);

		bsc.set("this_action", this);

		bsc.executeFunction(functionName);
	}
}