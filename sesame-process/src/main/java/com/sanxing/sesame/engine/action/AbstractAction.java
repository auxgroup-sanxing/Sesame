package com.sanxing.sesame.engine.action;

import com.sanxing.sesame.engine.action.cutpoint.ActionCutpoint;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.DehydrateManager;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.exceptions.XPathException;
import com.sanxing.sesame.engine.xpath.XPathUtil;
import com.sanxing.sesame.exceptions.AppException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.VariableContext;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.Text;

public abstract class AbstractAction implements Action {
	static final Logger LOG = LoggerFactory.getLogger(AbstractAction.class);

	static ThreadLocal<Map<String, XPath>> xpathCache = new ThreadLocal();
	private NamespaceContext nsCtx;
	List<ActionCutpoint> cutPoints = new LinkedList();
	private Element actionEl;

	private Map<String, XPath> getXPathCache() {
		if (xpathCache.get() == null) {
			xpathCache.set(new HashMap());
		}
		return ((Map) xpathCache.get());
	}

	public NamespaceContext getNamespaceContext() {
		return this.nsCtx;
	}

	public void setNamespaceContext(NamespaceContext nsCtx) {
		this.nsCtx = nsCtx;
	}

	public String getActionId() {
		return this.actionEl.getAttributeValue("id");
	}

	public String getName() {
		return this.actionEl.getName();
	}

	public void addActionCutPoint(ActionCutpoint cut) {
		this.cutPoints.add(cut);
	}

	public void init(Element config) {
		this.actionEl = config;
		for (int i = 0; i < this.cutPoints.size(); ++i) {
			ActionCutpoint cutpoint = (ActionCutpoint) this.cutPoints.get(i);
			cutpoint.beforeInit(config);
		}
		doinit(config);
		for (int i = this.cutPoints.size() - 1; i > -1; --i) {
			ActionCutpoint cutpoint = (ActionCutpoint) this.cutPoints.get(i);
			cutpoint.afterInit(config);
		}
	}

	public abstract void doinit(Element paramElement);

	public void dehydrate(ExecutionContext ctx) {
		DehydrateManager.dehydrate(ctx.getUuid(), getActionId(), ctx);
	}

	public void work(DataContext ctx) {
		// TODO:
	}

	public boolean isRollbackable() {
		return false;
	}

	public abstract void dowork(DataContext paramDataContext)
			throws AppException;

	public void doworkInDehydrateState(DataContext context) {
	}

	public Variable getVariable(DataContext ctx, String varName, String xPath) {
		Variable var = ctx.getVariable(varName);
		if (var == null)
			throw new ActionException("unknown var in context..." + varName);
		Variable variable;
		if ((var.getVarType() != 0) || (xPath == null) || (xPath.length() == 0)) {
			variable = var;
		} else {
			Element ele = (Element) var.get();
			variable = select(ele, xPath, ctx);
		}
		return variable;
	}

	public Variable select(Element startPoint, String strPath,
			VariableContext variableContext) {
		XPath xpath = null;
		try {
			xpath = getXPath(strPath);
			xpath.setVariableContext(variableContext);

			int type = 0;
			Variable result = null;
			List list = xpath.selectNodes(startPoint);

			if (list.size() > 1) {
				result = new Variable(list, 5);
			} else if (list.size() == 1) {
				Object first = list.get(0);

				if (first instanceof Element)
					type = 0;
				else if (first instanceof Boolean)
					type = 6;
				else if (first instanceof String)
					type = 7;
				else if (first instanceof Attribute)
					type = 3;
				else if (first instanceof Text)
					type = 1;
				else if (first instanceof CDATA)
					type = 2;
				else if (first instanceof Number) {
					type = 8;
				}

				result = new Variable(first, type);
			}

			return result;
		} catch (Exception e) {
			throw new XPathException(e.getMessage(),
					(e.getCause() != null) ? e.getCause() : e);
		}
	}

	XPath getXPath(String strPath) throws JaxenException {
		if (!(getXPathCache().containsKey(strPath))) {
			JDOMXPath result = new JDOMXPath(strPath);
			result.setNamespaceContext(this.nsCtx);
			SimpleNamespaceContext simpleNSCtx = (SimpleNamespaceContext) result
					.getNamespaceContext();
			for (String prefix : XPathUtil.commonNameSpace.keySet()) {
				String uri = (String) XPathUtil.commonNameSpace.get(prefix);
				simpleNSCtx.addNamespace(prefix, uri);
			}

			result.setFunctionContext(XPathUtil.fc);
			getXPathCache().put(strPath, result);
		}
		return ((XPath) getXPathCache().get(strPath));
	}
}