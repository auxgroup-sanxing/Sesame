package com.sanxing.sesame.component.params;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom.Element;

public class SUParameters extends HashMap<String, Parameter> {
	private static final long serialVersionUID = -351643054831856629L;
	private String suName;
	private Map<String, OperationParameters> operationParameters = new ConcurrentHashMap();

	public SUParameters(String suName) {
		this.suName = suName;
	}

	public String getSuName() {
		return this.suName;
	}

	public List<String> getSUParamKeys() {
		List keys = new LinkedList();
		keys.addAll(keySet());
		return keys;
	}

	public List<String> getOperationParamKeys(String operationName) {
		List keys = new LinkedList();
		OperationParameters op = (OperationParameters) this.operationParameters
				.get(operationName);
		if (op == null)
			return keys;
		keys.addAll(op.keySet());
		return keys;
	}

	public OperationParameters getOperationParams(String operationName) {
		return ((OperationParameters) this.operationParameters
				.get(operationName));
	}

	public OperationParameters addOperationParams(String operationName,
			OperationParameters value) {
		return ((OperationParameters) this.operationParameters.put(
				operationName, value));
	}

	public OperationParameters removeOperationParams(String operationName) {
		return ((OperationParameters) this.operationParameters
				.remove(operationName));
	}

	public Element toElement() {
		Element root = new Element("su-params");
		root.setAttribute("name", this.suName);
		Iterator keys = keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Parameter param = (Parameter) get(key);
			root.addContent(param.toElement());
		}

		Iterator operationNames = this.operationParameters.keySet().iterator();
		while (operationNames.hasNext()) {
			String operationName = (String) operationNames.next();
			OperationParameters op = (OperationParameters) this.operationParameters
					.get(operationName);
			Element operationParamElement = op.toElement();
			root.addContent(operationParamElement);
		}

		return root;
	}

	public static SUParameters fromElement(Element element) {
		SUParameters sup = new SUParameters(element.getAttributeValue("name"));
		List<Element> elements = element.getChildren("param");
		Parameter param;
		for (Element paramEle : elements) {
			param = Parameter.fromElement(paramEle);
			sup.put(param.getName(), param);
		}
		List<Element> operationElements = element
				.getChildren("operation-params");
		for (Element operationElement : operationElements) {
			OperationParameters op = OperationParameters
					.fromElement(operationElement);
			sup.addOperationParams(op.getOperationName(), op);
		}
		return sup;
	}
}