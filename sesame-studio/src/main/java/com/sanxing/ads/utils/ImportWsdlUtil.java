package com.sanxing.ads.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class ImportWsdlUtil {
	private static String urlRgx = "^(\\w+):\\/\\/([^/:]+)(:\\d*)?([^#   ]*)";

	private static Definition modifyWSDL(Definition def, String path)
			throws Exception {
		def.addNamespace("sn", "http://www.sanxing.com/ns/statenet");

		createXSD(def, path);

		addIncludeSchema(def, path);

		addExtensibilityElements(def);

		addExtensibilityAttribute(def);
		return def;
	}

	private static void addIncludeSchema(Definition def, String path)
			throws Exception {
		List newSchema = def.getTypes().getExtensibilityElements();
		int schemaSize = newSchema.size();
		for (int j = 0; j < schemaSize; ++j) {
			def.getTypes().removeExtensibilityElement(
					(ExtensibilityElement) newSchema.get(0));
		}

		Iterator portTypes = def.getPortTypes().keySet().iterator();
		String schemaString = "<schema targetNamespace=\""
				+ def.getTargetNamespace()
				+ "\" xmlns=\"http://www.w3.org/2001/XMLSchema\">";
		try {
			while (portTypes.hasNext()) {
				QName portTypeName = (QName) portTypes.next();
				PortType porttype = def.getPortType(portTypeName);

				List<Operation> operationList = porttype.getOperations();
				for (Operation operation : operationList) {
					String childSchema = "<include schemaLocation=\""
							+ operation.getName() + ".xsd\"/>";

					schemaString = schemaString + "\n" + childSchema;
				}
			}
			schemaString = schemaString + "\n" + "</schema>";
			Element schemaW3CElement = string2Element(schemaString);
			UnknownExtensibilityElement schemaElement = new UnknownExtensibilityElement();
			schemaElement.setElement(schemaW3CElement);
			schemaElement.setRequired(Boolean.valueOf(false));
			schemaElement.setElementType(new QName("http://ws.ictic.com",
					"schema"));
			def.getTypes().addExtensibilityElement(schemaElement);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static void addExtensibilityAttribute(Definition def) {
		Iterator services = def.getServices().keySet().iterator();
		while (services.hasNext()) {
			QName servicename = (QName) services.next();
			Service service = def.getService(servicename);
			Iterator ports = service.getPorts().keySet().iterator();
			while (ports.hasNext()) {
				String portname = (String) ports.next();
				Port port = service.getPort(portname);
				QName qName = new QName("http://www.sanxing.com/ns/statenet",
						"style", "art");
				port.setExtensionAttribute(qName, "");
			}
		}
	}

	private static void addExtensibilityElements(Definition def)
			throws Exception {
		Iterator bindingNames = def.getBindings().keySet().iterator();
		try {
			while (bindingNames.hasNext()) {
				QName bindingName = (QName) bindingNames.next();
				Binding bind = (Binding) def.getBindings().get(bindingName);
				String qname = bind.getPortType().getQName().getLocalPart();
				String bindNsURI = bind.getQName().getNamespaceURI();
				bind.setQName(new QName(bindNsURI, qname + "-binding"));
				UnknownExtensibilityElement extElement = new UnknownExtensibilityElement();
				String snBinding = "<sn:binding component-name=\"\" transport=\"\" type=\"\" xpath=\"\" head=\"\" signSwitch=\"\" encryptionSwitch=\"\" verifySwitch=\"\" signingKeyProvider=\"\" encryptionKeyProvider=\"\" verifyKeyProvider=\"\"/>";

				Element element = string2Element(snBinding);
				extElement.setElement(element);
				extElement.setRequired(Boolean.valueOf(true));
				extElement.setElementType(new QName(
						"http://www.sanxing.com/ns/statenet", "binding"));
				bind.addExtensibilityElement(extElement);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static Element string2Element(String snBinding)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
		ByteArrayInputStream stringInputStream = new ByteArrayInputStream(
				snBinding.getBytes());
		Document doc = bf.newDocumentBuilder().parse(stringInputStream);
		return doc.getDocumentElement();
	}

	private static void createXSD(Definition def, String unitPath)
			throws Exception {
		Iterator portTypes = def.getPortTypes().keySet().iterator();
		try {
			while (portTypes.hasNext()) {
				QName portTypeName = (QName) portTypes.next();
				PortType porttype = def.getPortType(portTypeName);

				List<Operation> operationList = porttype.getOperations();
				for (Operation operation : operationList) {
					String xsdPath = unitPath + File.separator
							+ operation.getName() + ".xsd";
					File xsdFile = new File(xsdPath);
					xsdFile.createNewFile();
					FileOutputStream out = new FileOutputStream(xsdFile);
					StreamResult xsdResult = new StreamResult(out);
					TransformerFactory transFactory = TransformerFactory
							.newInstance();
					Transformer transFormer = transFactory.newTransformer();
					String schemaString = "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\""
							+ def.getTargetNamespace()
							+ "\" elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" attributeFormDefault=\"unqualified\" targetNamespace=\""
							+ def.getTargetNamespace() + "\"/>";

					Element schemaElement = string2Element(schemaString);

					Iterator inputports = operation.getInput().getMessage()
							.getParts().keySet().iterator();
					while (inputports.hasNext()) {
						Part inputport = operation.getInput().getMessage()
								.getPart((String) inputports.next());
						Element inputelement = getSchemaElment(def,
								inputport.getElementName());
						schemaElement.appendChild(schemaElement
								.getOwnerDocument().importNode(
										inputelement.cloneNode(true), true));
					}

					Iterator outpurports = operation.getOutput().getMessage()
							.getParts().keySet().iterator();
					while (outpurports.hasNext()) {
						Part outputport = operation.getOutput().getMessage()
								.getPart((String) outpurports.next());
						Element outputelement = getSchemaElment(def,
								outputport.getElementName());
						schemaElement.appendChild(schemaElement
								.getOwnerDocument().importNode(
										outputelement.cloneNode(true), true));
					}
					DOMSource domSource = new DOMSource(schemaElement);
					transFormer.transform(domSource, xsdResult);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private static Element getSchemaElment(Definition def, QName elementName)
			throws XPathExpressionException {
		List schemas = def.getTypes().getExtensibilityElements();
		Iterator i$ = schemas.iterator();
		if (i$.hasNext()) {
			Schema schema = (Schema) i$.next();
			Element schemaElement = schema.getElement();
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression xpathExpr = xpath
					.compile("//*[local-name()='element' and @name = '"
							+ elementName.getLocalPart() + "']");
			NodeList nodeList = (NodeList) xpathExpr.evaluate(schemaElement,
					XPathConstants.NODESET);
			return ((Element) nodeList.item(0));
		}
		return null;
	}

	private static WSDLReader getWsdlReader() throws WSDLException {
		WSDLFactory wsdlFactory = WSDLFactory.newInstance();
		WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
		wsdlReader.setFeature("javax.wsdl.verbose", false);
		wsdlReader.setFeature("javax.wsdl.importDocuments", true);
		return wsdlReader;
	}

	private static WSDLWriter getWsdlWriter() throws WSDLException {
		WSDLFactory wsdlF = WSDLFactory.newInstance();
		WSDLWriter wsdlWriter = wsdlF.newWSDLWriter();
		return wsdlWriter;
	}

	private static void modifyNS(Definition def) {
		def.removeNamespace("");
		def.removeNamespace("wsdl");
		def.removeNamespace("wsdlsoap");

		def.addNamespace("sn", "http://www.sanxing.com/ns/statenet");
		def.addNamespace("xs", "http://www.w3.org/2001/XMLSchema");
		def.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
		def.addNamespace("", "http://schemas.xmlsoap.org/wsdl/");
	}

	private static void createDefDocumentation(Definition def)
			throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		Element element = document.createElementNS(
				"http://schemas.xmlsoap.org/wsdl/", "documentation");
		Text textNode = document.createTextNode("");
		element.appendChild(textNode);

		def.setDocumentationElement(element);
	}

	public static void WSDLAnalyser(String importWsdlPath, String unitPath)
			throws Exception {
		WSDLReader wsdlReader = getWsdlReader();
		boolean isURL = importWsdlPath.matches(urlRgx);
		String wsdlFileName = unitPath + File.separator + "unit.wsdl";
		if ((isURL) || (new File(importWsdlPath).exists())) {
			Definition def = wsdlReader.readWSDL(importWsdlPath);
			modifyWSDL(def, unitPath);
			modifyNS(def);
			createDefDocumentation(def);

			getWsdlWriter().writeWSDL(def,
					new FileOutputStream(new File(wsdlFileName)));
		}
	}
}