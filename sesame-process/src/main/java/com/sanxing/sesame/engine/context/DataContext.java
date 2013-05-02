package com.sanxing.sesame.engine.context;

import com.sanxing.sesame.engine.action.var.VarNotFoundException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;

public class DataContext implements VariableContext, Cloneable {
	private static Logger LOG = LoggerFactory.getLogger(DataContext.class);
	private String uuid;
	private Map<String, Variable> variables = new HashMap();
	private ExecutionContext executionContext;

	public void close() {
		this.variables.clear();
	}

	public void addVariable(String name, Variable var) {
		this.variables.put(name, var);
	}

	public void put(String name, String value) {
		this.variables.put(name, new Variable(value, 7));
	}

	public void put(String name, boolean value) {
		this.variables.put(name, new Variable(Boolean.valueOf(value), 6));
	}

	public void put(String name, long value) {
		this.variables.put(name, new Variable(Long.valueOf(value), 8));
	}

	public void put(String name, int value) {
		this.variables.put(name, new Variable(Integer.valueOf(value), 8));
	}

	public void put(String name, double value) {
		this.variables.put(name, new Variable(Double.valueOf(value), 8));
	}

	public void put(String name, BigDecimal value) {
		this.variables.put(name, new Variable(value, 8));
	}

	public Variable findVariable(String name) {
		return ((Variable) this.variables.get(name));
	}

	public Variable getVariable(String name) {
		if (!(this.variables.containsKey(name))) {
			throw new VarNotFoundException("No variable [" + name
					+ "] in context");
		}
		return ((Variable) this.variables.get(name));
	}

	public void delVariable(String name) {
		this.variables.remove(name);
	}

	private DataContext(String _uuid) {
		this.uuid = _uuid;
	}

	public String getUuid() {
		return this.uuid;
	}

	public static DataContext getInstance() {
		return new DataContext(UUIDGenerator.getUUID());
	}

	public static DataContext getInstance(String uuid) {
		return new DataContext(uuid);
	}

	public Object clone() throws CloneNotSupportedException {
		DataContext result = new DataContext(UUIDGenerator.getUUID());
		Iterator keys = this.variables.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Variable var = (Variable) this.variables.get(key);
			Variable newVar = (Variable) var.clone();
			result.addVariable(key, newVar);
		}
		result.setExecutionContext(getExecutionContext());
		return result;
	}

	public Object getVariableValue(String namespaceURI, String prefix,
			String localName) throws UnresolvableException {
		Variable var = (Variable) this.variables.get(localName);
		if (var == null) {
			for (String varName : this.variables.keySet()) {
				LOG.debug("var in context [" + varName + "]");
			}
			throw new UnresolvableException("Variable not found: {prefix="
					+ prefix + ", namespaceURI=" + namespaceURI + "}"
					+ localName);
		}
		return var.get();
	}

	public ExecutionContext getExecutionContext() {
		return this.executionContext;
	}

	void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public Map<String, Variable> getVariables() {
		return this.variables;
	}
}