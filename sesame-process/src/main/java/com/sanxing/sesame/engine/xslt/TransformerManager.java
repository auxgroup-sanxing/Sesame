package com.sanxing.sesame.engine.xslt;

import com.sanxing.sesame.engine.ExecutionEnv;
import com.sanxing.sesame.engine.action.ActionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;

public class TransformerManager {
	private static Map<Object, XSLTransformer> transformerCache = new Hashtable();
	private static Map<Object, Templates> templatesCache = new Hashtable();

	private static Map<String, String> extensionCache = new HashMap();
	private static TransformerFactory factory;

	private static TransformerFactory getTransformerFactory() {
		return (TransformerManager.factory = TransformerFactory.newInstance());
	}

	public static XSLTransformer getInstance(File file) {
		try {
			if (transformerCache.get(file) == null) {
				InputStream xsltStream = new FileInputStream(file);
				XSLTransformer transformer = new XSLTransformer(xsltStream);
				transformerCache.put(file, transformer);
			}
			return ((XSLTransformer) transformerCache.get(file));
		} catch (XSLTransformException e) {
			throw new ActionException(e, "00008");
		} catch (IOException ie) {
			throw new ActionException(ie, "00007");
		}
	}

	public static Transformer getTransformer(Element configEl,
			List<Namespace> namespaces) {
		try {
			Templates templates = (Templates) templatesCache.get(configEl);
			if (templates == null) {
				String xsltText = configEl.getText();
				Reader xsltReader = new StringReader(xsltText);
				Document xsltDoc = new SAXBuilder().build(xsltReader);
				Element rootEl = xsltDoc.getRootElement();
				Set<String> keys = extensionCache.keySet();
				String uri;
				Namespace ns;
				for (String key : keys) {
					uri = "xalan://" + ((String) extensionCache.get(key));
					ns = Namespace.getNamespace(key, uri);
					rootEl.addNamespaceDeclaration(ns);
				}
				String prefixes = keys.toString().replaceAll("^\\[|\\,|\\]$",
						"");
				rootEl.setAttribute("extension-element-prefixes", prefixes);

				for (Namespace namespace : namespaces) {
					rootEl.addNamespaceDeclaration(namespace);
				}

				Map<String, String> env = ExecutionEnv.export();
				for (String name : env.keySet()) {
					Element paramEl = new Element("param",
							rootEl.getNamespace());
					paramEl.setAttribute("name", name);
					rootEl.addContent(paramEl);
				}

				Source xsltSource = new JDOMSource(xsltDoc);
				templates = getTransformerFactory().newTemplates(xsltSource);
				templatesCache.put(configEl, templates);
			}
			return templates.newTransformer();
		} catch (Exception e) {
			throw new ActionException(e, "00008");
		}
	}

	public static XSLTransformer getXSLTransformer(String path, String xsltText) {
		try {
			XSLTransformer transformer = (XSLTransformer) transformerCache
					.get(path);
			if (transformer == null) {
				Reader xsltReader = new StringReader(xsltText);
				Document xsltDoc = new SAXBuilder().build(xsltReader);
				Element rootEl = xsltDoc.getRootElement();
				Set<String> keys = extensionCache.keySet();
				for (String key : keys) {
					String uri = "xalan://"
							+ ((String) extensionCache.get(key));
					Namespace ns = Namespace.getNamespace(key, uri);
					rootEl.addNamespaceDeclaration(ns);
				}
				String prefixes = keys.toString().replaceAll("^\\[|\\,|\\]$",
						"");
				rootEl.setAttribute("extension-element-prefixes", prefixes);

				transformer = new XSLTransformer(xsltDoc);
				transformerCache.put(path, transformer);
			}
			return transformer;
		} catch (Exception e) {
			throw new ActionException(e, "00008");
		}
	}

	public static void clearCache(String groupId) {
		transformerCache.clear();
		templatesCache.clear();
	}

	public static void registerExtension(String prefix, String className) {
		extensionCache.put(prefix, className);
	}
}