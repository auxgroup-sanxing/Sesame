package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.Iterator;
import java.util.List;
import org.jdom2.Element;

public class IfThenAction extends AbstractAction implements Constant {
	String varName;
	String xpath;
	Element config;

	public void doinit(Element config) {
		this.varName = config.getAttributeValue("var");

		this.xpath = config.getChildTextTrim("xpath");

		this.config = config;
	}

	public void dowork(DataContext ctx) {
		Variable var = ctx.getVariable(this.varName);
		Element ele = (Element) var.get();

		Variable booleanVar = select(ele, this.xpath, ctx);
		Boolean bool = (Boolean) booleanVar.get();
		if (bool.booleanValue()) {
			Iterator thenActions = this.config.getChild("then").getChildren()
					.iterator();
			ActionUtil.bachInvoke(ctx, thenActions);
		} else {
			boolean elseif = false;

			List list = this.config.getChildren("else-if");

			for (int i = 0; i < list.size(); ++i) {
				Element elseifthen = (Element) list.get(i);

				this.varName = elseifthen.getAttributeValue("var");
				String xpath = elseifthen.getChildTextTrim("xpath");

				var = ctx.getVariable(this.varName);
				ele = (Element) var.get();

				booleanVar = select(ele, xpath, ctx);
				bool = (Boolean) booleanVar.get();
				Iterator thenActions = elseifthen.getChild("then")
						.getChildren().iterator();
				if (bool.booleanValue()) {
					elseif = true;
					ActionUtil.bachInvoke(ctx, thenActions);
					break;
				}
			}

			if ((!(elseif)) && (this.config.getChild("else") != null)) {
				Iterator elseActions = this.config.getChild("else")
						.getChildren().iterator();
				ActionUtil.bachInvoke(ctx, elseActions);
			}
		}
	}
}