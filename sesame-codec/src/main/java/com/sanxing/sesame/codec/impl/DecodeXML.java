package com.sanxing.sesame.codec.impl;

import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLResult;
import java.io.InputStream;
import org.jdom2.input.SAXBuilder;

public class DecodeXML implements Decoder {
	private ThreadLocal<SAXBuilder> localBuilder = new ThreadLocal();

	public void destroy() {
	}

	public void init(String workspaceRoot) {
	}

	private SAXBuilder getSAXBuilder() {
		SAXBuilder builder = (SAXBuilder) this.localBuilder.get();
		if (builder == null) {
			this.localBuilder.set(builder = new SAXBuilder());
			((SAXBuilder) this.localBuilder.get()).setFastReconfigure(true);
		}
		return builder;
	}

	public void decode(BinarySource source, XMLResult result)
			throws FormatException {
		try {
			if (source.getReader() != null) {
				result.setDocument(getSAXBuilder().build(source.getReader()));
				return;
			}
			InputStream stream = source.getInputStream();
			if (stream != null) {
				result.setDocument(getSAXBuilder().build(stream));
				return;
			}

			result.setDocument(getSAXBuilder().build(source.getSystemId()));
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
	}
}