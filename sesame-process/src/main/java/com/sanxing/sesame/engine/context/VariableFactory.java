package com.sanxing.sesame.engine.context;

import com.sanxing.sesame.engine.action.ActionException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;

public class VariableFactory {
	private static Map<String, Element> rawValueCache = new HashMap();

	public static Variable getIntance(String rawValue, String type) {
		try {
			if (type.equalsIgnoreCase("element")) {
				if (!(rawValueCache.containsKey(rawValue))) {
					SAXBuilder builder = new SAXBuilder();
					Document doc = builder.build(new StringReader(rawValue));
					Element root = doc.getRootElement();
					rawValueCache.put(rawValue, root);
				}
				return new Variable(
						((Element) rawValueCache.get(rawValue)).clone(), 0);
			}
			if (type.equalsIgnoreCase("namespace")) {
				Namespace ns = Namespace.getNamespace(
						rawValue.substring(0, rawValue.indexOf(":")),
						rawValue.substring(rawValue.indexOf(":") + 1));
				return new Variable(ns, 4);
			}
			if (type.equalsIgnoreCase("attribute")) {
				String[] temp = rawValue.split(":");
				Attribute attr = new Attribute(temp[0], temp[1]);
				return new Variable(attr, 3);
			}
			if (type.equalsIgnoreCase("text")) {
				Text text = new Text(rawValue);
				return new Variable(text, 1);
			}
			if (type.equalsIgnoreCase("string")) {
				return new Variable(rawValue, 7);
			}
			if (type.equalsIgnoreCase("cdata")) {
				CDATA cdata = new CDATA(rawValue);
				return new Variable(cdata, 2);
			}
			throw new ActionException("variable type [" + type
					+ "] is not supported");
		} catch (IOException e) {
			throw new RuntimeException();
		} catch (JDOMException e) {
			throw new ActionException("can not parse your raw value ["
					+ rawValue + "] to " + type + " varviable ");
		}
	}

	public static Variable getInstanceByObj(Object obj) {
		if (obj instanceof Number) {
			return new Variable(obj, 8);
		}
		if (obj instanceof String)
			return new Variable(obj, 7);
		if (obj instanceof Boolean)
			return new Variable(obj, 6);
		if (obj instanceof List)
			return new Variable(obj, 5);
		if (obj instanceof Element) {
			return new Variable(obj, 0);
		}
		return null;
	}
}