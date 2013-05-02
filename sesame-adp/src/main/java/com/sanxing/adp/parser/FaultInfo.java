package com.sanxing.adp.parser;

import java.util.HashMap;
import java.util.Map;

public class FaultInfo {
	private String name;
	private Map<String, PartInfo> parts = new HashMap();

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addPart(PartInfo part) {
		this.parts.put(part.getName(), part);
	}

	public Map<String, PartInfo> getParts() {
		return this.parts;
	}
}