package com.sanxing.sesame.dispatch.cluster;

import com.sanxing.sesame.servicedesc.InternalEndpoint;

public abstract interface ClusterEndpointChooser {
	public abstract InternalEndpoint choose(
			InternalEndpoint paramInternalEndpoint,
			InternalEndpoint[] paramArrayOfInternalEndpoint);
}