package com.sanxing.sesame.engine.action.transform;

import com.sanxing.sesame.engine.ExecutionEnv;
import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.xslt.TransformerManager;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;

public class XslTransformAction extends AbstractAction {
	private String xsltPath;
	private String toVarName;
	private Element actionEl;
	private List<Namespace> namespaces;

	public void doinit(Element actionEl) {
		this.namespaces = actionEl.getDocument().getRootElement()
				.getAdditionalNamespaces();
		this.actionEl = actionEl;
		this.xsltPath = getPath(actionEl);
		this.toVarName = actionEl.getAttributeValue("to-var");
	}

	private String getPath(Element configEl) {
		int index = 0;
		String path = "";
		for (Element el = configEl; el != null; el = el.getParentElement()) {
			index = (el.getParentElement() != null) ? el.getParentElement()
					.indexOf(el) : 0;
			path = "/" + el.getName() + "[" + index + "]" + path;
		}
		return path;
	}

	public void dowork(DataContext ctx) {
		ClassLoader loader = (ClassLoader) ctx.getExecutionContext().get(
				"process.classloader");
		ClassLoader savedCl = Thread.currentThread().getContextClassLoader();
		try {
			if (loader != null) {
				Thread.currentThread().setContextClassLoader(loader);
			}

			Transformer transformer = TransformerManager.getTransformer(
					this.actionEl, this.namespaces);

			Map<String, String> env = ExecutionEnv.export();
			for (String name : env.keySet()) {
				Object value = ctx.getExecutionContext().get(
						(String) env.get(name));
				transformer.setParameter(name, (value != null) ? value : "");
			}

			Document document = new Document();
			Element contextEl = new Element("context");
			document.setRootElement(contextEl);
			Set<Map.Entry<String, Variable>> variables = ctx.getVariables()
					.entrySet();
			for (Map.Entry var : variables) {
				String varName = (String) var.getKey();
				Variable variable = (Variable) var.getValue();
				if (variable.getVarType() == 0) {
					Element elem = (Element) variable.get();
					Document src = elem.getDocument();
					if (src == null)
						src = new Document(elem);
					Element varEl = (Element) elem.clone();
					varEl.setName(varName);
					varEl.setNamespace(Namespace.NO_NAMESPACE);
					contextEl.addContent(varEl);
				}
			}
			JDOMSource source = new JDOMSource(document);
			JDOMResult result = new JDOMResult();
			transformer.transform(source, result);
			Document resultDoc = result.getDocument();
			Element root = resultDoc.getRootElement();
			Variable resultVar = new Variable(root, 0);
			ctx.addVariable(this.toVarName, resultVar);
		} catch (TransformerException e) {
			throw new RuntimeException(e.getMessage(),
					(e.getCause() != null) ? e.getCause() : e);
		} finally {
			Thread.currentThread().setContextClassLoader(savedCl);
		}
	}
}