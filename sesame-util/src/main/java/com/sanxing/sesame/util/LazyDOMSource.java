package com.sanxing.sesame.util;

import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Node;

public abstract class LazyDOMSource extends DOMSource {
	private boolean initialized;

	public Node getNode() {
		if (!(this.initialized)) {
			setNode(loadNode());
			this.initialized = true;
		}
		return super.getNode();
	}

	protected abstract Node loadNode();
}