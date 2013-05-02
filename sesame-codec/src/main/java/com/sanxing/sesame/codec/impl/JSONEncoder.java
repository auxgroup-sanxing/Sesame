package com.sanxing.sesame.codec.impl;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLSource;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONEncoder implements Encoder {
	public void init(String workspaceRoot) {
	}

	public void encode(XMLSource source, BinaryResult result)
			throws FormatException {
		try {
			Document document = source.getJDOMDocument();
			Element rootEl = document.getRootElement();

			JSONObject object = new JSONObject();

			iterate(rootEl, object);

			result.write(object.toString().getBytes(result.getEncoding()));
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
	}

	private void iterate(Element element, JSONObject object)
			throws JSONException {
		List<Attribute> attributes = element.getAttributes();
		for (Attribute attribute : attributes) {
			object.put(attribute.getName(), attribute.getValue());
		}

		List<Element> children = element.getChildren();
		for (Element child : children)
			if ((child.getAttributes().isEmpty())
					&& (child.getChildren().isEmpty())) {
				object.append(child.getName(), child.getText());
			} else {
				JSONObject json = new JSONObject();
				object.append(child.getName(), json);
				iterate(child, json);
			}
	}

	public void destroy() {
	}
}