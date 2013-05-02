package com.sanxing.sesame.dispatch.cluster;

import com.sanxing.sesame.servicedesc.InternalEndpoint;
import java.io.PrintStream;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomCEChooser implements ClusterEndpointChooser {
	private static Logger LOG = LoggerFactory.getLogger(RandomCEChooser.class);
	Random generator;

	public RandomCEChooser() {
		this.generator = new Random();
	}

	public InternalEndpoint choose(InternalEndpoint localPoint,
			InternalEndpoint[] remotes) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("there are " + remotes.length + " endpoints");
		}

		if (localPoint.getComponentNameSpace() == null) {
			int index = this.generator.nextInt(remotes.length);
			return remotes[index];
		}

		int index = this.generator.nextInt(remotes.length + 1);

		if (index == 0) {
			return localPoint;
		}
		return remotes[(index - 1)];
	}

	public static void main(String[] args) {
		Random generator = new Random();
		for (int i = 0; i < 1000; ++i)
			System.out.println(generator.nextInt(2));
	}
}