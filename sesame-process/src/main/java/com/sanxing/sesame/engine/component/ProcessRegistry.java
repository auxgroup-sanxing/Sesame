package com.sanxing.sesame.engine.component;

import com.sanxing.sesame.engine.FlowInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessRegistry {
	private Map<String, FlowInfo> flows = new ConcurrentHashMap();

	public void put(String operationName, FlowInfo flow) {
		this.flows.put(operationName, flow);
	}

	public FlowInfo get(String operationName) {
		return ((FlowInfo) this.flows.get(operationName));
	}

	public void clear() {
		this.flows.clear();
	}
}