package com.sanxing.adp.parser;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationInfo {
	private String operationName;
	private List<PartInfo> params = new LinkedList();

	private List<PartInfo> results = new LinkedList();

	private List<FaultInfo> faults = new LinkedList();
	private String description;
	private static Logger LOG = LoggerFactory.getLogger(OperationInfo.class);

	static Logger logger = LoggerFactory.getLogger(OperationInfo.class);

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOperationName() {
		return this.operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getCapOperationName() {
		return StringUtils.capitalise(this.operationName);
	}

	public boolean isVoid() {
		return (this.results.size() != 1);
	}

	public void addParamter(PartInfo param) {
		this.params.add(param);
	}

	public void addResult(PartInfo param) {
		this.results.add(param);
	}

	public void addFault(FaultInfo fault) {
		this.faults.add(fault);
	}

	public boolean isMultipleReturnParts() {
		return (this.results.size() > 1);
	}

	public PartInfo getResult() {
		if (!(isVoid())) {
			return ((PartInfo) this.results.get(0));
		}
		return null;
	}

	public int getMethodParamCount() {
		if (!(isVoid())) {
			return this.params.size();
		}
		return (this.params.size() + this.results.size());
	}

	public List<PartInfo> getParams() {
		return this.params;
	}

	public void setParams(List<PartInfo> params) {
		this.params = params;
	}

	public List<PartInfo> getResults() {
		return this.results;
	}

	public void setResults(List<PartInfo> results) {
		this.results = results;
	}

	public List<FaultInfo> getFaults() {
		return this.faults;
	}

	public void setFaults(List<FaultInfo> faults) {
		this.faults = faults;
	}

	public void init4runtime(PortTypeInfo portType, ClassLoader load)
			throws Exception {
	}
}