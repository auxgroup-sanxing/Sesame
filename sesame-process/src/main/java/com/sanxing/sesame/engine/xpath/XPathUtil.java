package com.sanxing.sesame.engine.xpath;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.jaxen.Function;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPathFunctionContext;

public class XPathUtil {
	static Map xpaths = new HashMap();

	static Map xpathsResultTypes = new HashMap();
	static GenericKeyedObjectPool xpathPool;
	public static XPathFunctionContext fc = new XPathFunctionContext(true);

	/** @deprecated */
	public static SimpleNamespaceContext nc = new SimpleNamespaceContext();

	public static Map<String, String> commonNameSpace = new ConcurrentHashMap();

	public static void registerFunction(String namespaceUri, String prefix,
			String funcName, Function function) {
		nc.addNamespace(prefix, namespaceUri);
		commonNameSpace.put(prefix, namespaceUri);
		fc.registerFunction(namespaceUri, funcName, function);
	}
}