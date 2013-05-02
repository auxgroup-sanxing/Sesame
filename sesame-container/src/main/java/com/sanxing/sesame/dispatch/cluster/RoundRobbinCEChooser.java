package com.sanxing.sesame.dispatch.cluster;

import com.sanxing.sesame.servicedesc.InternalEndpoint;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobbinCEChooser implements ClusterEndpointChooser {
	private InternalEndpoint localPoint;
	private AtomicInteger next = new AtomicInteger(0);

	private AtomicInteger limit = new AtomicInteger(0);

	public RoundRobbinCEChooser(InternalEndpoint localPoint) {
		this.localPoint = localPoint;

		check(localPoint);
	}

	private void check(InternalEndpoint localPoint) {
		if (localPoint.getComponentNameSpace() == null) {
			if (localPoint.getRemoteEndpoints().length != this.limit.get()) {
				this.limit.set(localPoint.getRemoteEndpoints().length);
			}
		} else if (localPoint.getRemoteEndpoints().length + 1 != this.limit
				.get())
			this.limit.set(localPoint.getRemoteEndpoints().length + 1);
	}

	private void reset() {
		this.next.set(0);
	}

	public InternalEndpoint choose(InternalEndpoint localPoint,
			InternalEndpoint[] remotes) {
		check(localPoint);
		int cursor = this.next.getAndIncrement();
		if (cursor < this.limit.get()) {
			if (this.localPoint.getComponentNameSpace() == null) {
				return remotes[cursor];
			}
			if (cursor == 0) {
				return localPoint;
			}
			return remotes[(cursor - 1)];
		}

		reset();
		return choose(localPoint, remotes);
	}
}