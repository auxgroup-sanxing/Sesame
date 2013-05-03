package com.sanxing.sesame.engine.action.var;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

public class RemoveNSAction extends AbstractAction implements Constant {
	String targetVarName;
	String sourceVarName;

	public void doinit(Element config) {
		this.targetVarName = config.getAttributeValue("to-var");
		this.sourceVarName = config.getAttributeValue("var");
		if (this.targetVarName == null)
			this.targetVarName = this.sourceVarName;
	}

	public void dowork(DataContext ctx) {
		try {
			Variable toBeRemoved = getVariable(ctx, this.sourceVarName, null);
			if (toBeRemoved.getVarType() == 0) {
				Element ele = (Element) toBeRemoved.get();
				removeNameSpace(ele);
			}
		} catch (Exception localException) {
		}
	}

	private void removeNameSpace(Element ele) {
		Namespace ns = ele.getNamespace();

		if (ns != null) {
			ele.setNamespace(Namespace.NO_NAMESPACE);

			for (int i = 0; i < ele.getChildren().size(); ++i)
				removeNameSpace((Element) ele.getChildren().get(i));
		}
	}
}