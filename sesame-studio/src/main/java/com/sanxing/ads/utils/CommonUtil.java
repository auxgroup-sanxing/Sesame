package com.sanxing.ads.utils;

import org.jdom.input.SAXBuilder;

public class CommonUtil {
	public static SAXBuilder newSAXBuilder() {
		SAXBuilder builder = new SAXBuilder(
				"org.apache.xerces.parsers.SAXParser", false);
		builder.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd",
				false);
		return builder;
	}

	public static SAXBuilder newSAXBuilder(boolean validate) {
		SAXBuilder builder = new SAXBuilder(
				"org.apache.xerces.parsers.SAXParser", validate);
		builder.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd",
				false);
		return builder;
	}
}