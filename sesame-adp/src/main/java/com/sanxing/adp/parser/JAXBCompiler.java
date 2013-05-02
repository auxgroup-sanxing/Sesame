package com.sanxing.adp.parser;

import com.sanxing.adp.util.FileUtil;
import com.sun.tools.xjc.Driver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.wsdl.Definition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPath;

public class JAXBCompiler {
	private Logger LOG = LoggerFactory.getLogger(JAXBCompiler.class);
	private SchemaHolder schema;

	public JAXBCompiler(SchemaHolder schemaHolder) {
		this.schema = schemaHolder;
	}

	public void jaxbCompile(String targetDir, String packageName)
			throws Exception {
		List<Element> schemaElements = this.schema.getSchemaJDOMElements();
		for (Element element : schemaElements)
			compile(element, packageName, targetDir);
	}

	private void compile(Element schemaElement, String packageName,
			String targetDir) throws Exception {
		try {
			String baseURI = this.schema.def.getDocumentBaseURI();
			Namespace xs = Namespace
					.getNamespace("http://www.w3.org/2001/XMLSchema");
			List schemas = schemaElement.getChildren("include", xs);

			String tempXSD = FileUtil.getTargetDir(baseURI) + File.separator
					+ "temp.xsd";
			FileWriter writer = new FileWriter(tempXSD);
			XMLOutputter output = new XMLOutputter();
			output.output(schemaElement, writer);
			writer.flush();
			writer.close();
			try {
				String[] args = (String[]) null;

				if ((packageName == null) || (packageName.equals("")))
					args = new String[] { "-nv", "-no-header", "-d", targetDir,
							tempXSD };
				else {
					args = new String[] { "-nv", "-no-header", "-p",
							packageName, "-d", targetDir, tempXSD };
				}
				Driver.run(args, System.err, System.out);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			this.LOG.error("", e);
			throw e;
		}
	}

	private void replace(String fileName) throws IOException, JDOMException {
		File file = new File(fileName);
		FileInputStream input = new FileInputStream(file);
		try {
			Element element = new SAXBuilder().build(input).getRootElement();
			Namespace nsJaxb = Namespace.getNamespace("jaxb",
					"http://java.sun.com/xml/ns/jaxb");
			element.addNamespaceDeclaration(nsJaxb);
			element.setAttribute(new Attribute("version", "2.1", nsJaxb));
			XPath path = XPath
					.newInstance("//xsd:element/xsd:annotation/xsd:documentation");
			path.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
			List docs = path.selectNodes(element);
			Namespace nsXsd = Namespace
					.getNamespace("http://www.w3.org/2001/XMLSchema");

			for (int i = 0; i < docs.size(); ++i) {
				Element docElement = (Element) docs.get(i);
				String description = docElement.getText();
				Element appinfo = null;
				appinfo = docElement.getParentElement().getChild("appinfo",
						nsXsd);
				if (appinfo != null) {
					Element property = appinfo.getChild("property", nsJaxb);
					if (property != null) {
						Element javadoc = property.getChild("javadoc", nsJaxb);
						if (javadoc != null) {
							javadoc.setText(description);
						} else {
							javadoc = new Element("javadoc", nsJaxb);
							javadoc.setText(description);
							property.addContent(javadoc);
						}
					} else {
						property = new Element("property", nsJaxb);
						appinfo.addContent(property);
						Element javadoc = new Element("javadoc", nsJaxb);
						javadoc.setText(description);
						property.addContent(javadoc);
						appinfo.addContent(property);
					}
				} else {
					appinfo = new Element("appinfo", nsXsd);
					Element property = new Element("property", nsJaxb);
					appinfo.addContent(property);
					Element javadoc = new Element("javadoc", nsJaxb);
					javadoc.setText(description);
					property.addContent(javadoc);
					((Element) docElement.getParent()).addContent(appinfo);
				}

			}

			FileOutputStream outStream = new FileOutputStream(file);
			try {
				XMLOutputter output = new XMLOutputter();
				output.setFormat(Format.getPrettyFormat().setEncoding("utf-8"));
				output.output(element, outStream);
			} finally {
				outStream.flush();
				outStream.close();
			}
		} finally {
			input.close();
		}
	}
}