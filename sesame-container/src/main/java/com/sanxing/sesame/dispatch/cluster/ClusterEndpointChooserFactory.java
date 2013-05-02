package com.sanxing.sesame.dispatch.cluster;

import com.sanxing.sesame.servicedesc.InternalEndpoint;
import java.util.HashMap;
import java.util.Map;

public class ClusterEndpointChooserFactory {
	static ClusterEndpointChooser random = new RandomCEChooser();

	private static Map<InternalEndpoint, ClusterEndpointChooser> roundRobbinCache = new HashMap();

	public static ClusterEndpointChooser random() {
		return random;
	}

	public static ClusterEndpointChooser roundRobbin(
			InternalEndpoint localEndpoint) {
		if (!(localEndpoint.isClustered())) {
			throw new RuntimeException("unsupported ep , must be clusted");
		}
		if (!(roundRobbinCache.containsKey(localEndpoint))) {
			roundRobbinCache.put(localEndpoint, new RoundRobbinCEChooser(
					localEndpoint));
		}
		return ((ClusterEndpointChooser) roundRobbinCache.get(localEndpoint));
	}
}