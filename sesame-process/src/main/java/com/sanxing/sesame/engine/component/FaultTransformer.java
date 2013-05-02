package com.sanxing.sesame.engine.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Document;
import org.jdom2.Element;

public class FaultTransformer {
	private static Logger LOG = LoggerFactory.getLogger(FaultTransformer.class);

	private Map<String, Element> mapping = new HashMap();

	public FaultTransformer(Document descriptor) throws IOException {
		Element templateEl = descriptor.getRootElement();

		List<Element> cases = templateEl.getChildren("when",
				templateEl.getNamespace());
		for (Element whenEl : cases)
			this.mapping.put(whenEl.getAttributeValue("status"), whenEl);
	}

	public boolean hasEntry() {
		return (!(this.mapping.isEmpty()));
	}

	public Document transform(String status, String statusText) {
		Element rootEl = new Element("fault");
		Element codeEl = new Element("fault-code");
		String faultCode = null;
		String faultReason = null;
		Element entry = (Element) this.mapping.get(status);
		if (entry == null) {
			entry = (Element) this.mapping.get("*");
		}
		if (entry == null) {
			faultCode = status;
			faultReason = statusText;
		} else {
			faultCode = entry.getAttributeValue("fault-code", status);
			faultReason = entry.getAttributeValue("fault-reason", statusText);
		}
		codeEl.setText(faultCode);
		rootEl.addContent(codeEl);
		Element reasonEl = new Element("fault-reason");
		reasonEl.setText(faultReason);
		rootEl.addContent(reasonEl);
		Document result = new Document(rootEl);

		return result;
	}
}