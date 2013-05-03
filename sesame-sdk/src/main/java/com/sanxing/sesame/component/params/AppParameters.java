package com.sanxing.sesame.component.params;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.util.SystemProperties;

public class AppParameters extends HashMap<String, Parameter> {
	private static final long serialVersionUID = 2155756403680656125L;
	private static Logger LOG = LoggerFactory.getLogger(AppParameters.class);

	private Map<String, SUParameters> suParameters = new ConcurrentHashMap();
	private static AppParameters instance;

	void addSUParameter(SUParameters sup) {
		this.suParameters.put(sup.getSuName(), sup);
	}

	public List<String> getAppParamKeys() {
		List keys = new LinkedList();
		keys.addAll(keySet());
		return keys;
	}

	public List<String> getSuParamKeys(String suName) {
		List list = new ArrayList();
		SUParameters sup = (SUParameters) this.suParameters.get(suName);
		if (sup != null)
			list = sup.getSUParamKeys();
		return list;
	}

	public List<String> getOperationParamKeys(String suName, String operation) {
		List list = new ArrayList();
		SUParameters sup = (SUParameters) this.suParameters.get(suName);
		if (sup != null)
			list = sup.getOperationParamKeys(operation);
		return list;
	}

	public void remove(String paramName) {
		super.remove(paramName);
	}

	public void remove(String suName, String paramName) {
		SUParameters sup = (SUParameters) this.suParameters.get(suName);
		if (sup != null)
			sup.remove(paramName);
	}

	public void remove(String suName, String operationName, String paramName) {
		SUParameters sup = (SUParameters) this.suParameters.get(suName);
		if (sup != null) {
			OperationParameters op = sup.getOperationParams(operationName);
			if (op != null)
				op.remove(paramName);
		}
	}

	public Parameter getParamter(String suName, String operationName,
			String paramName) {
		SUParameters suParam = (SUParameters) this.suParameters.get(suName);
		if (suParam != null) {
			OperationParameters op = suParam.getOperationParams(operationName);
			if (op != null) {
				Parameter result = (Parameter) op.get(paramName);
				if (result != null) {
					return result;
				}
			}
			Parameter result = (Parameter) suParam.get(paramName);
			if (result != null) {
				return result;
			}
		}
		return ((Parameter) get(paramName));
	}

	public Parameter getOperationParamter(String suName, String operationName,
			String paramName) {
		SUParameters suParam = (SUParameters) this.suParameters.get(suName);
		if (suParam != null) {
			OperationParameters op = suParam.getOperationParams(operationName);
			if (op != null) {
				Parameter result = (Parameter) op.get(paramName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public Parameter getSUParamter(String suName, String paramName) {
		SUParameters suParam = (SUParameters) this.suParameters.get(suName);
		if (suParam != null) {
			return ((Parameter) suParam.get(paramName));
		}
		return null;
	}

	public Parameter getAppParamter(String paramName) {
		return ((Parameter) get(paramName));
	}

	public void setParameter(String suName, String operationName,
			String paramName, String value, Parameter.PARAMTYPE type,
			String comment) {
		Parameter param = Parameter.newParameter(type);
		param.setComment(comment);
		param.setName(paramName);
		param.setValue(value);
		SUParameters sup = (SUParameters) this.suParameters.get(suName);
		if (sup == null) {
			sup = new SUParameters(suName);
			this.suParameters.put(suName, sup);
		}
		OperationParameters op = sup.getOperationParams(operationName);
		if (op == null) {
			op = new OperationParameters(operationName);
			sup.addOperationParams(operationName, op);
		}
		op.put(paramName, param);
	}

	public void setParameter(String suName, String paramName, String value,
			Parameter.PARAMTYPE type, String comment) {
		Parameter param = Parameter.newParameter(type);
		param.setComment(comment);
		param.setName(paramName);
		param.setValue(value);
		SUParameters sup = (SUParameters) this.suParameters.get(suName);
		if (sup == null) {
			sup = new SUParameters(suName);
			this.suParameters.put(suName, sup);
		}
		sup.put(paramName, param);
	}

	public void setParameter(String paramName, String value,
			Parameter.PARAMTYPE type, String comment) {
		Parameter param = Parameter.newParameter(type);
		param.setComment(comment);
		param.setName(paramName);
		param.setValue(value);
		put(paramName, param);
	}

	Element toElement() {
		Element root = new Element("sesame-biz-params");
		Iterator keys = keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Parameter param = (Parameter) get(key);
			root.addContent(param.toElement());
		}
		Iterator suNames = this.suParameters.keySet().iterator();
		while (suNames.hasNext()) {
			String suName = (String) suNames.next();
			System.out.println("su name........." + suName);
			SUParameters sup = (SUParameters) this.suParameters.get(suName);
			root.addContent(sup.toElement());
		}
		return root;
	}

	static void buildFromElement(Element root) {
		instance.suParameters.clear();
		instance.clear();
		List<Element> elements = root.getChildren("param");
		Parameter param;
		for (Element paramEle : elements) {
			param = Parameter.fromElement(paramEle);
			instance.put(param.getName(), param);
		}

		List<Element> suParamElements = root.getChildren("su-params");
		for (Element paramEle : suParamElements) {
			SUParameters sup = SUParameters.fromElement(paramEle);
			instance.suParameters.put(sup.getSuName(), sup);
		}
	}

	public static AppParameters getInstance() {
		if (instance == null) {
			instance = new AppParameters();
			try {
				Document doc = new SAXBuilder().build(SystemProperties
						.get("SESAME_HOME") + "/conf/biz-params.xml");
				buildFromElement(doc.getRootElement());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public void store() {
		try {
			XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
			FileOutputStream fos = new FileOutputStream(
					SystemProperties.get("SESAME_HOME")
							+ "/conf/biz-params.xml");
			output.output(toElement(), fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}