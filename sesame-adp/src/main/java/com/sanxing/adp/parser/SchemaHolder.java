package com.sanxing.adp.parser;

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.sanxing.adp.util.FileUtil;
import com.sanxing.adp.util.WSDLUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.input.DOMBuilder;
import org.jdom2.transform.JDOMSource;

public class SchemaHolder {
	Definition def;
	private XmlSchema schema;
	private Map<QName, XmlSchemaElement> types = new HashMap();
	private Map declaredNamespaces;
	private List<org.jdom2.Element> jdomSchemaElements = new LinkedList();
	private String targetDir;
	private static Logger LOG = LoggerFactory.getLogger(SchemaHolder.class);

	public XmlSchema getSchema() {
		return this.schema;
	}

	public Map<QName, XmlSchemaElement> getTypes() {
		return this.types;
	}

	public SchemaHolder(Definition def) throws Exception {
		this.def = def;
		this.declaredNamespaces = def.getNamespaces();

		extractSchema(def);
	}

	private void extractSchema(Definition wsdlDefinition) throws Exception {
		org.w3c.dom.Element w3cSchemaElement = null;
		if (wsdlDefinition.getTypes() != null) {
			List<ExtensibilityElement> schemaExtElements = WSDLUtil
					.findExtensibilityElement(wsdlDefinition.getTypes()
							.getExtensibilityElements(), "schema");
			for (ExtensibilityElement schemaElement : schemaExtElements) {
				w3cSchemaElement = ((SchemaImpl) schemaElement).getElement();
				if (w3cSchemaElement != null) {
					DOMBuilder domBuilder = new DOMBuilder();
					org.jdom2.Element jdomSchemaElement = domBuilder
							.build(w3cSchemaElement);
					jdom2Schema(jdomSchemaElement);
				}
			}
		} else {
			Map imports = wsdlDefinition.getImports();
			Iterator keys = imports.keySet().iterator();

			while (keys.hasNext()) {
				Vector importV = (Vector) imports.get(keys.next());
				Iterator importIter = importV.iterator();
				while (importIter.hasNext()) {
					Import impor = (Import) importIter.next();

					this.declaredNamespaces.putAll(impor.getDefinition()
							.getNamespaces());
					this.def = impor.getDefinition();

					extractSchema(impor.getDefinition());
				}
			}
		}
	}

	private void jdom2Schema(org.jdom2.Element schemaElement) throws Exception {
		if ((this.declaredNamespaces != null)
				&& (!(this.declaredNamespaces.isEmpty()))) {
			Iterator nsIter = this.declaredNamespaces.keySet().iterator();
			while (nsIter.hasNext()) {
				String nsPrefix = (String) nsIter.next();
				String nsURI = (String) this.declaredNamespaces.get(nsPrefix);
				if ((nsPrefix != null) && (nsPrefix.length() > 0)) {
					Namespace nsDecl = Namespace.getNamespace(nsPrefix, nsURI);
					schemaElement.addNamespaceDeclaration(nsDecl);
				}
			}
		}
		schemaElement.detach();
		this.jdomSchemaElements.add(schemaElement);
		XmlSchemaCollection schemaCol = new XmlSchemaCollection();

		schemaCol.setBaseUri(FileUtil.getTargetDir(this.def
				.getDocumentBaseURI()));
		this.schema = schemaCol.read(
				new JDOMSource(new Document(schemaElement)), null);

		parseSchema(this.schema);
	}

	public List<org.jdom2.Element> getSchemaJDOMElements() {
		return this.jdomSchemaElements;
	}

	private void parseSchema(XmlSchema schema) throws Exception {
		Iterator iterElementNames = schema.getElements().getValues();
		while (iterElementNames.hasNext()) {
			XmlSchemaElement ele = (XmlSchemaElement) iterElementNames.next();
			this.types.put(ele.getQName(), ele);
		}
		XmlSchemaObjectCollection schemaCollection = schema.getIncludes();
		if (schemaCollection != null) {
			Iterator iter = schemaCollection.getIterator();
			while (iter.hasNext()) {
				XmlSchemaExternal include = (XmlSchemaExternal) iter.next();
				parseSchema(include.getSchema());
			}
		}
	}
}