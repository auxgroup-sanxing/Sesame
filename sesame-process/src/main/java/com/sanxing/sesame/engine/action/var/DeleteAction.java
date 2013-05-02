package com.sanxing.sesame.engine.action.var;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class DeleteAction extends AbstractAction implements Constant {
	String varName;
	String xpath;

	public void doinit(Element config) {
		this.varName = config.getAttributeValue("var");
		this.xpath = config.getChildTextTrim("xpath");
	}

	public void dowork(DataContext ctx) {
		Variable toBeDeleted = ctx.getVariable(this.varName);
		if (this.xpath != null)
			if (this.xpath.equalsIgnoreCase("xmlns")) {
				Element ele = (Element) toBeDeleted.get();
				removeNameSpace(ele);
			} else {
				toBeDeleted = select((Element) toBeDeleted.get(), this.xpath,
						ctx);
				if (toBeDeleted.getVarType() <= 2) {
					((Content) toBeDeleted.get()).detach();
				} else if (toBeDeleted.getVarType() == 3) {
					((Attribute) toBeDeleted.get()).detach();
				} else if (toBeDeleted.getVarType() == 5) {
					List list = (List) toBeDeleted.get();
					for (int i = 0; i < list.size(); ++i) {
						Element ele = (Element) list.get(i);
						ele.detach();
					}
				}
			}
		else
			ctx.delVariable(this.varName);
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