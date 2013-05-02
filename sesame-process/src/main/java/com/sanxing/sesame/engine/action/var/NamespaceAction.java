package com.sanxing.sesame.engine.action.var;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import java.util.Iterator;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class NamespaceAction extends AbstractAction implements Constant {
	private String method;
	private Namespace namespace;
	private String varName;
	private String xpath;

	public void doinit(Element config) {
		try {
			this.method = config.getAttributeValue("method", "set");

			String prefix = config.getAttributeValue("prefix", "");
			String uri = config.getAttributeValue("uri", "");
			this.namespace = Namespace.getNamespace(prefix, uri);

			this.varName = config.getAttributeValue("var");
			this.xpath = config.getChildTextTrim("xpath");
		} catch (Exception e) {
			throw new ActionException(this, "00001");
		}
	}

	public void dowork(DataContext ctx) {
		try {
			Variable target = getVariable(ctx, this.varName, this.xpath);
			if (this.method.equals("set")) {
				setNamespace(target);
				return;
			}
			if (this.method.equals("add")) {
				addNamespace(target);
				return;
			}
			if (this.method.equals("remove"))
				removeNamespace(target);
		} catch (ActionException e) {
			throw e;
		} catch (Exception e) {
			throw new ActionException(this, e);
		}
	}

	private void removeNamespace(Variable target) {
		if (target.getVarType() == 0) {
			Element el = (Element) target.get();
			if (this.namespace.getURI().length() == 0) {
				Namespace ns = el.getNamespace(this.namespace.getPrefix());
				el.removeNamespaceDeclaration(ns);
			} else {
				el.removeNamespaceDeclaration(this.namespace);
			}
		} else if (target.getVarType() == 5) {
			List list = (List) target.get();
			for (Iterator localIterator = list.iterator(); localIterator
					.hasNext();) {
				Object obj = localIterator.next();
				if (obj instanceof Element) {
					Element el = (Element) obj;
					el.removeNamespaceDeclaration(this.namespace);
				}
			}
		} else {
			throw new ActionException(this, "00004");
		}
	}

	private void addNamespace(Variable target) {
		if (target.getVarType() == 0) {
			Element el = (Element) target.get();
			el.addNamespaceDeclaration(this.namespace);
		} else if (target.getVarType() == 5) {
			List list = (List) target.get();
			for (Iterator localIterator = list.iterator(); localIterator
					.hasNext();) {
				Object obj = localIterator.next();
				if (obj instanceof Element) {
					Element el = (Element) obj;
					el.addNamespaceDeclaration(this.namespace);
				}
			}
		} else {
			throw new ActionException(this, "00005");
		}
	}

	private void setNamespace(Variable target) {
		if (target.getVarType() == 0) {
			Element el = (Element) target.get();
			el.setNamespace(this.namespace);
		} else if (target.getVarType() == 3) {
			Attribute attribute = (Attribute) target.get();
			attribute.setNamespace(this.namespace);
		} else if (target.getVarType() == 5) {
			List list = (List) target.get();
			for (Iterator localIterator = list.iterator(); localIterator
					.hasNext();) {
				Object obj = localIterator.next();
				if (obj instanceof Element) {
					Element el = (Element) obj;
					el.setNamespace(this.namespace);
				} else if (obj instanceof Attribute) {
					Attribute attribute = (Attribute) obj;
					attribute.setNamespace(this.namespace);
				}
			}
		} else {
			throw new ActionException(this, "00006");
		}
	}
}