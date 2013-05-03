package com.sanxing.sesame.engine.action.var;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

public class RenameAction extends AbstractAction implements Constant {
	String varName;
	String xpath;
	String newName;

	public void doinit(Element config) {
		this.varName = config.getAttributeValue("var");
		this.xpath = config.getAttributeValue("xpath");
		this.newName = config.getAttributeValue("new-name");
	}

	public void dowork(DataContext ctx) {
		Variable toBeRenamed = getVariable(ctx, this.varName, this.xpath);
		if (toBeRenamed.getVarType() == 0) {
			((Element) toBeRenamed.get()).setName(this.newName);
		} else if (toBeRenamed.getVarType() == 3) {
			((Attribute) toBeRenamed.get()).setName(this.newName);
		} else if (toBeRenamed.getVarType() == 5) {
			List list = (List) toBeRenamed.get();
			for (int i = 0; i < list.size(); ++i) {
				Element ele = (Element) list.get(i);
				ele.setName(this.newName);
			}
		}
	}
}