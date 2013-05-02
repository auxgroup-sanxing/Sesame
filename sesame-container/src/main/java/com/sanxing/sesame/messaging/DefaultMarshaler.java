package com.sanxing.sesame.messaging;

import com.sanxing.sesame.jaxp.StringSource;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Node;

public class DefaultMarshaler implements PojoMarshaler {
	private PojoMarshaler parent;

	public DefaultMarshaler() {
	}

	public DefaultMarshaler(PojoMarshaler parent) {
		this.parent = parent;
	}

	public PojoMarshaler getParent() {
		return this.parent;
	}

	public void marshal(MessageExchange exchange, NormalizedMessage message,
			Object body) throws MessagingException {
		if (body instanceof Source) {
			message.setContent((Source) body);
		} else {
			message.setProperty("com.sanxing.sesame.body", body);
			Source content = asContent(message, body);
			message.setContent(content);
		}
	}

	public Object unmarshal(MessageExchange exchange, NormalizedMessage message)
			throws MessagingException {
		Object answer = message.getProperty("com.sanxing.sesame.body");
		if (answer == null) {
			if (this.parent != null) {
				answer = this.parent.unmarshal(exchange, message);
			}
			if (answer == null) {
				answer = defaultUnmarshal(exchange, message);
			}
		}
		return answer;
	}

	protected Object defaultUnmarshal(MessageExchange exchange,
			NormalizedMessage message) {
		Source content = message.getContent();
		if (content instanceof DOMSource) {
			DOMSource source = (DOMSource) content;
			return source.getNode();
		}
		return content;
	}

	protected Source asContent(NormalizedMessage message, Object body) {
		if (body instanceof Source)
			return ((Source) body);
		if (body instanceof String) {
			return new StringSource((String) body);
		}
		if (body instanceof Node) {
			return new DOMSource((Node) body);
		}
		return null;
	}
}