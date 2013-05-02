package com.sanxing.ads.utils;

import com.ibm.wsdl.TypesImpl;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaSerializer.XmlSchemaSerializerException;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLMerge {
	private static final Logger log = Logger.getLogger(WSDLMerge.class);

	public static void main(String[] args) {
	}

	public Definition merge(Definition source)
			throws XmlSchemaSerializerException {
		cleanNamespace(source);

		combineImport(source);

		return source;
	}

	private void cleanNamespace(Definition source) {
		Iterator bindingNames = source.getBindings().keySet().iterator();
		while (bindingNames.hasNext()) {
			QName bindingName = (QName) bindingNames.next();
			Binding bind = (Binding) source.getBindings().get(bindingName);
			if ((bind != null) && (bind.getPortType() != null)) {
				bind.getPortType().setQName(
						new QName(source.getTargetNamespace(), bind
								.getPortType().getQName().getLocalPart()));

				if (log.isDebugEnabled()) {
					log.debug("portype name changed to "
							+ bind.getPortType().getQName());
				}
				bind.getPortType().setUndefined(false);
				bind.getPortType().getOperations();
				for (int i = 0; i < bind.getPortType().getOperations().size(); ++i) {
					Operation operation = (Operation) bind.getPortType()
							.getOperations().get(i);
					if (operation.getInput() != null) {
						QName inputMsgName = new QName(
								source.getTargetNamespace(), operation
										.getInput().getMessage().getQName()
										.getLocalPart());

						operation.getInput().getMessage()
								.setQName(inputMsgName);
						if (log.isDebugEnabled()) {
							log.debug("input param's name changed to "
									+ operation.getInput().getMessage()
											.getQName());
						}
					}
					if (operation.getOutput() != null) {
						QName outputMsgName = new QName(
								source.getTargetNamespace(), operation
										.getOutput().getMessage().getQName()
										.getLocalPart());

						operation.getOutput().getMessage()
								.setQName(outputMsgName);
						if (log.isDebugEnabled()) {
							log.debug("out param's name changed to "
									+ operation.getOutput().getMessage()
											.getQName());
						}
					}
					Iterator faultIterator = operation.getFaults().keySet()
							.iterator();
					while (faultIterator.hasNext()) {
						Object faultkey = faultIterator.next();
						Fault fault = (Fault) operation.getFaults().get(
								faultkey);
						Message faultMSG = fault.getMessage();
						String faultLocalName = faultMSG.getQName()
								.getLocalPart();
						QName faultQname = new QName(
								source.getTargetNamespace(), faultLocalName);
						faultMSG.setQName(faultQname);
						if (log.isDebugEnabled())
							log.debug("fault param's name changed to "
									+ faultMSG.getQName());
					}
				}
			}
		}
	}

	private void combineImport(Definition source)
			throws XmlSchemaSerializerException {
		if (source.getImports().size() != 0) {
			Iterator iterImports = source.getImports().keySet().iterator();
			while (iterImports.hasNext()) {
				String importQName = (String) iterImports.next();
				if (log.isDebugEnabled())
					log.debug("handling import .........." + importQName);
				Vector wsdlImports = (Vector) source.getImports().get(
						importQName);
				for (int i = 0; i < wsdlImports.size(); ++i) {
					Import wsdlImport = (Import) wsdlImports.get(0);
					source.removeImport(wsdlImport);
					Definition importDef = wsdlImport.getDefinition();
					Definition mergedDef = merge(importDef);
					if (source.getTypes() != null) {
						List schemas = source.getTypes()
								.getExtensibilityElements();
						int schemaSize = schemas.size();
						for (int j = 0; j < schemaSize; ++j) {
							Schema importSchema = (Schema) schemas.get(j);
							moveSchema(source, importSchema);
						}

						List newSchema = source.getTypes()
								.getExtensibilityElements();
						for (int j = 0; j < schemaSize; ++j) {
							source.getTypes().removeExtensibilityElement(
									(ExtensibilityElement) newSchema.get(0));
						}

						List mergedSchemas = mergedDef.getTypes()
								.getExtensibilityElements();
						for (int j = 0; j < mergedSchemas.size(); ++j) {
							Schema importSchema = (Schema) mergedSchemas.get(j);
							moveSchema(source, importSchema);
						}
					} else {
						source.setTypes(new TypesImpl());
						Types importTypes = mergedDef.getTypes();
						if (importTypes != null) {
							List schemas = mergedDef.getTypes()
									.getExtensibilityElements();
							for (int j = 0; j < schemas.size(); ++j) {
								Schema importSchema = (Schema) schemas.get(j);
								moveSchema(source, importSchema);
							}
						}

					}

					source.getMessages().putAll(mergedDef.getMessages());
					source.getPortTypes().putAll(mergedDef.getAllPortTypes());
					source.getAllBindings().putAll(mergedDef.getAllBindings());
					source.getAllServices().putAll(mergedDef.getAllServices());
				}
			}
		} else {
			if (source.getTypes() == null)
				return;
			List schemas = source.getTypes().getExtensibilityElements();
			int schemaSize = schemas.size();
			for (int j = 0; j < schemaSize; ++j) {
				Schema importSchema = (Schema) schemas.get(j);
				moveSchema(source, importSchema);
			}

			List newSchema = source.getTypes().getExtensibilityElements();
			for (int j = 0; j < schemaSize; ++j)
				source.getTypes().removeExtensibilityElement(
						(ExtensibilityElement) newSchema.get(0));
		}
	}

	private void moveSchema(Definition source, Schema importSchema)
			throws XmlSchemaSerializerException {
		Element schemaElement = importSchema.getElement();
		XmlSchemaCollection schemaCol = new XmlSchemaCollection();
		schemaCol.read(schemaElement, schemaElement.getBaseURI());
		for (int j = 0; j < schemaCol.getXmlSchemas().length; ++j)
			try {
				XmlSchema schema = schemaCol.getXmlSchemas()[j];
				NamespaceMap namespaceContext = new NamespaceMap();
				namespaceContext.add("", "http://www.w3.org/2001/XMLSchema");
				namespaceContext.add("tns", schema.getTargetNamespace());
				schema.setNamespaceContext(namespaceContext);
				SchemaImpl wsdlSchema = new SchemaImpl();
				Element w3cdoc4schema = schema.getSchemaDocument()
						.getDocumentElement();
				if (w3cdoc4schema.getChildNodes().getLength() > 1) {
					source.getTypes().addExtensibilityElement(wsdlSchema);
				}
				NodeList importList = w3cdoc4schema
						.getElementsByTagName("import");
				while (importList.getLength() > 0) {
					Node node = importList.item(0);
					node.getParentNode().removeChild(node);
				}
				NodeList includeList = w3cdoc4schema
						.getElementsByTagName("include");
				while (includeList.getLength() > 0) {
					Node node = includeList.item(0);
					node.getParentNode().removeChild(node);
				}
				wsdlSchema.setElement(w3cdoc4schema);

				wsdlSchema.setElementType(new QName(
						"http://www.w3.org/2001/XMLSchema", "schema"));
			} catch (XmlSchemaSerializerException e) {
				throw e;
			}
	}
}