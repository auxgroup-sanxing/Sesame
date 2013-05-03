package com.sanxing.studio.search;

import com.sanxing.studio.utils.WSDLMerge;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAnnotated;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLParser {
	private XmlSchemaCollection schemaCollection;
	private Map<QName, XmlSchema> schemaMap;
	private Map<String, Record> projectRecordMap;
	private Map<String, String> serviceUnitMap;
	static Logger logger = LoggerFactory.getLogger(WSDLParser.class.getName());

	public WSDLParser() {
		this.schemaCollection = new XmlSchemaCollection();

		this.schemaMap = new HashMap();

		this.projectRecordMap = new HashMap();

		this.serviceUnitMap = new HashMap();
	}

	public Definition getDefinition(File file) {
		try {
			WSDLFactory wsdlFactory = WSDLFactory.newInstance();
			WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
			wsdlReader.setFeature("javax.wsdl.verbose", false);
			wsdlReader.setFeature("javax.wsdl.importDocuments", true);
			Definition def = wsdlReader.readWSDL(file.toURL().toString());
			return def;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getImportLocation(Definition wsdlDef) {
		StringBuffer strBuffer = new StringBuffer();
		Map imports = wsdlDef.getImports();
		for (Iterator iter = imports.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Vector<Import> victor = (Vector<Import>) entry.getValue();
			for (Import wsdlImport : victor) {
				if (wsdlImport.getLocationURI() != null) {
					strBuffer.append(wsdlImport.getLocationURI() + "#");
				}
			}
		}
		return strBuffer.toString();
	}

	public void loadSchemas(Definition wsdlDef) throws IOException {
		Map imports = wsdlDef.getImports();
		for (Iterator iter = imports.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Vector<Import> victor = (Vector<Import>) entry.getValue();
			for (Import wsdlImport : victor) {
				loadSchemas(wsdlImport.getDefinition());
			}
		}

		if (wsdlDef.getTypes() == null) {
			return;
		}
		List list = wsdlDef.getTypes().getExtensibilityElements();
		for (int i = 0; i < list.size(); ++i) {
			ExtensibilityElement extEl = (ExtensibilityElement) list.get(i);
			if (extEl instanceof Schema) {
				org.w3c.dom.Element schemaEl = ((Schema) extEl).getElement();
				XmlSchemaCollection schemaCollection2 = new XmlSchemaCollection();
				XmlSchema schema = schemaCollection2.read(schemaEl,
						wsdlDef.getDocumentBaseURI());

				XmlSchemaObjectTable eles = schema.getElements();
				Iterator iterator = eles.getNames();
				while (iterator.hasNext()) {
					QName name = (QName) iterator.next();
					this.schemaMap.put(name, schema);
					logger.debug("add schema map: " + name);
				}

				XmlSchemaObjectTable types = schema.getSchemaTypes();
				iterator = types.getNames();
				while (iterator.hasNext()) {
					QName name = (QName) iterator.next();
					this.schemaMap.put(name, schema);
					logger.debug("add schema map: " + name);
				}
			}
		}
	}

	public XmlSchema getSchema(QName name) {
		return ((XmlSchema) this.schemaMap.get(name));
	}

	public Iterator<?> getElements(XmlSchemaType schemaType) {
		XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
		if (complexType.getParticle() instanceof XmlSchemaSequence) {
			XmlSchemaSequence xsdSequence = (XmlSchemaSequence) complexType
					.getParticle();

			XmlSchemaObjectCollection coll = xsdSequence.getItems();
			Iterator elements = coll.getIterator();
			return elements;
		}
		return null;
	}

	public String getSchemaDescription(XmlSchemaAnnotated object) {
		XmlSchemaAnnotation ann = null;
		ann = object.getAnnotation();

		if (ann != null) {
			XmlSchemaObjectCollection collect = ann.getItems();
			for (int i = 0; i < collect.getCount(); ++i) {
				NodeList nl = null;
				XmlSchemaObject obj = collect.getItem(i);
				if (obj instanceof XmlSchemaDocumentation) {
					XmlSchemaDocumentation doc = (XmlSchemaDocumentation) collect
							.getItem(i);

					nl = doc.getMarkup();
				}
				if (nl == null) {
					continue;
				}

				for (int j = 0; j < nl.getLength(); ++j) {
					Node n = nl.item(j);
					if (n.getNodeType() == 3) {
						return n.getNodeValue();
					}
				}
			}

		}

		return null;
	}

	private void debugSchemaMap() {
		Iterator iterator = this.schemaMap.keySet().iterator();
		while (iterator.hasNext()) {
			QName name = (QName) iterator.next();
			logger.debug(name.toString());
		}
	}

	private void portTypeIndex(PortType type, Record base,
			RecordCollector recCollector) {
		if ((type == null) || (recCollector == null)) {
			logger.debug("PortType is null, It's may be not complete WSDL!");
			return;
		}

		List<Operation> operations = type.getOperations();
		for (Operation opera : operations) {
			logger.debug("operation name: " + opera.getName());

			OperationRecord operarec = new OperationRecord(opera);
			recCollector.addRecord(operarec, base);

			Input inputEle = opera.getInput();
			MessageRecord inrec;
			if (inputEle != null) {
				Message input = opera.getInput().getMessage();

				inrec = new MessageRecord(input);
				recCollector.addRecord(inrec, operarec);

				Map parts = input.getParts();
				for (Part part : (Collection<Part>) parts.values()) {
					logger.debug("part qname: " + part.getElementName());

					XmlSchema schema = getSchema(part.getElementName());

					if (schema != null) {
						XmlSchemaElement schemaEl = schema
								.getElementByName(part.getElementName());

						schemaIndex(schema, schemaEl.getSchemaType(), inrec,
								recCollector);
					} else {
						logger.debug("No schema found for "
								+ part.getElementName());

						debugSchemaMap();
					}
				}
			}

			Output outputEl = opera.getOutput();
			if (outputEl != null) {
				Message output = opera.getOutput().getMessage();

				MessageRecord outrec = new MessageRecord(output);
				recCollector.addRecord(outrec, operarec);

				Map parts = output.getParts();
				for (Part part : (Collection<Part>) parts.values()) {
					XmlSchema schema = getSchema(part.getElementName());
					if (schema != null) {
						XmlSchemaElement schemaEl = schema
								.getElementByName(part.getElementName());

						schemaIndex(schema, schemaEl.getSchemaType(), outrec,
								recCollector);
					} else {
						logger.debug("No schema found for "
								+ part.getElementName());

						debugSchemaMap();
					}
				}
			}
		}
	}

	public String getServiceType(File file) {
		if ((file.getParentFile() != null)
				&& (file.getParentFile().getParentFile() != null)
				&& (file.getParentFile().getParentFile().getName() != null)) {
			return file.getParentFile().getParentFile().getName();
		}

		return null;
	}

	public String getServiceUnitName(File wsdl) {
		if ((wsdl.getParentFile() != null)
				&& (wsdl.getParentFile().getName() != null)) {
			return wsdl.getParentFile().getName();
		}

		return null;
	}

	public File getProjJbiFile(File wsdl) {
		File file = new File(wsdl.getParentFile().getParentFile()
				.getParentFile(), "jbi.xml");

		return file;
	}

	public File getSuJbiFile(File wsdl) {
		File file = new File(wsdl.getParentFile(), "jbi.xml");
		return file;
	}

	public Record getProjectRecord(File jbi) {
		logger.debug("generate index for jbi file " + jbi.getAbsolutePath());
		try {
			String jbiPath = jbi.getAbsolutePath();
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(jbi);
			org.jdom.Element root = doc.getRootElement();
			Namespace compNs = root.getNamespace("comp");

			if (compNs != null) {
				String uri = (String) this.serviceUnitMap.get(jbiPath);
				if (uri == null) {
					this.serviceUnitMap.put(jbiPath, compNs.getURI());
					logger.debug("add map " + jbiPath);
				}
				return null;
			}

			Record record = (Record) this.projectRecordMap.get(jbiPath);
			if (record != null) {
				return record;
			}
			String projectName = null;
			String projectDesc = null;
			Namespace ns = root.getNamespace();
			org.jdom.Element sa = root.getChild("service-assembly", ns);
			if (sa != null) {
				org.jdom.Element identify = sa.getChild("identification", ns);
				if (identify != null) {
					org.jdom.Element name = identify.getChild("name", ns);
					org.jdom.Element desc = identify
							.getChild("description", ns);

					if (name != null) {
						projectName = name.getText();
					}
					if (desc != null) {
						projectDesc = desc.getText();
					}
				}
			}

			record = RecordFactory.createRecord("project");
			Column column = new Column("project name", projectName, true);

			record.addField(column);

			column = new Column("project description", projectDesc, true);

			record.addField(column);
			this.projectRecordMap.put(jbiPath, record);

			return record;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Record getStdRecord(File wsdl) {
		Definition definition = getDefinition(wsdl);

		Record record = RecordFactory.createRecord("std");
		Column column = new Column("import wsdl location",
				getImportLocation(definition), false);

		record.addField(column);

		column = new Column("service type", getServiceType(wsdl), false);

		record.addField(column);

		column = new Column("service unit", getServiceUnitName(wsdl), false);

		record.addField(column);

		File jbiFile = getSuJbiFile(wsdl);
		String component = (String) this.serviceUnitMap.get(jbiFile
				.getAbsolutePath());
		logger.debug(jbiFile.getAbsoluteFile().toString());
		logger.debug(component);
		column = new Column("component", component, false);
		record.addField(column);

		return record;
	}

	public RecordCollector getWSDLRecords(File file) {
		logger.debug("generate index for WSDL file " + file.getAbsolutePath());
		Definition definition = getDefinition(file);
		RecordCollector recCollector = null;
		try {
			boolean hasImport = false;
			if (definition.getImports().size() > 0) {
				hasImport = true;
			}
			recCollector = new RecordCollector();

			WSDLMerge wsdlMerge = new WSDLMerge();
			Definition def = wsdlMerge.merge(definition);

			loadSchemas(def);

			String serviceUnitDesc = null;
			if (def.getDocumentationElement() != null) {
				serviceUnitDesc = def.getDocumentationElement()
						.getTextContent();
			}

			Map services = def.getServices();
			ServiceRecord srec;
			if (services.size() > 0) {
				for (Service service : (Collection<Service>) services.values()) {
					logger.debug("service name: " + service.getQName());

					srec = new ServiceRecord(service, serviceUnitDesc);

					recCollector.addRecord(srec);

					Map ports = service.getPorts();
					for (Port port : (Collection<Port>) ports.values()) {
						Binding binding = port.getBinding();
						PortType type = binding.getPortType();

						InterfaceRecord intrec = new InterfaceRecord(port,
								binding, type);

						recCollector.addRecord(intrec, srec);

						if (hasImport) {
							logger.debug("It's proxy service built from engine service!");
						}

						portTypeIndex(type, intrec, recCollector);
					}
				}
			} else {
				srec = new ServiceRecord("", serviceUnitDesc);
				recCollector.addRecord(srec);
				Map types = def.getPortTypes();
				Iterator it = types.keySet().iterator();
				while (it.hasNext()) {
					Object o = it.next();
					Object obj = types.get(o);
					if (obj instanceof PortType)
						portTypeIndex((PortType) obj, srec, recCollector);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recCollector;
	}

	public void schemaIndex(XmlSchema schema, XmlSchemaType xsdType,
			Record base, RecordCollector recCollector) {
		if (xsdType instanceof XmlSchemaComplexType) {
			Iterator elements = getElements(xsdType);
			while (elements.hasNext()) {
				XmlSchemaElement element = (XmlSchemaElement) elements.next();

				QName ref = element.getRefName();
				XmlSchemaType xsType;
				if (ref != null) {
					XmlSchema refSchema = getSchema(ref);
					xsType = refSchema.getElementByName(ref).getSchemaType();
					element = refSchema.getElementByName(element.getRefName());
				} else {
					xsType = element.getSchemaType();

					if (xsType == null) {
						QName typeName = element.getSchemaTypeName();
						XmlSchema refSchema = getSchema(typeName);

						if (refSchema.getElementByName(typeName) != null) {
							xsType = refSchema.getElementByName(typeName)
									.getSchemaType();

							element = refSchema.getElementByName(element
									.getRefName());
						} else {
							xsType = refSchema.getTypeByName(typeName);
						}
					}
				}

				String description = getSchemaDescription(element);
				ElementRecord eleRec = new ElementRecord(element.getName(),
						description);

				recCollector.addRecord(eleRec, base);

				if (!(xsType instanceof XmlSchemaSimpleType))
					if (element.getSchemaType() instanceof XmlSchemaComplexType) {
						schemaIndex(schema, element.getSchemaType(), base,
								recCollector);
					} else if (xsType instanceof XmlSchemaComplexType)
						schemaIndex(schema, xsType, base, recCollector);
			}
		}
	}
}