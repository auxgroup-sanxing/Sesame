package com.sanxing.studio.utils;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

public class WSDLUtil {
	private static WSDLFactory factory;
	private static WSDLReader reader;
	private static WSDLWriter writer;

	public static WSDLReader getWSDLReader() throws WSDLException {
		if (reader != null)
			return reader;
		return newWSDLReader();
	}

	public static WSDLReader newWSDLReader() throws WSDLException {
		WSDLFactory wsdlFactory = getWSDLFactory();
		reader = wsdlFactory.newWSDLReader();
		reader.setFeature("javax.wsdl.verbose", false);
		reader.setFeature("javax.wsdl.importDocuments", false);
		return reader;
	}

	public static WSDLWriter getWSDLWriter() throws WSDLException {
		if (writer != null)
			return writer;
		WSDLFactory wsdlFactory = getWSDLFactory();
		writer = wsdlFactory.newWSDLWriter();
		return writer;
	}

	public static Definition newDefinition() throws WSDLException {
		return getWSDLFactory().newDefinition();
	}

	private static WSDLFactory getWSDLFactory() throws WSDLException {
		return (WSDLUtil.factory = WSDLFactory.newInstance());
	}
}