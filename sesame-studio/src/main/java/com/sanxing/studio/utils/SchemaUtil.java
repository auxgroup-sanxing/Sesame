package com.sanxing.studio.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SchemaUtil {
	static Element xmlRootElement;
	private static Map<String, String> namespaceMap = new HashMap();

	static String getDocumentation(Element element) {
		Element annoEl = element.getChild("annotation", element.getNamespace());
		if (annoEl == null)
			return "";
		return annoEl.getChildText("documentation", element.getNamespace());
	}

	static void getAppinfo(Element element, JSONObject component)
			throws JSONException {
		Namespace ns = element.getNamespace();
		Element annoEl;
		Element appEl;
		Iterator iter;
		if (((annoEl = element.getChild("annotation", ns)) != null)
				&& ((appEl = annoEl.getChild("appinfo", ns)) != null)) {
			List list = appEl.getChildren();
			for (iter = list.iterator(); iter.hasNext();) {
				Element child = (Element) iter.next();
				String value = child.getValue();
				if (value.equals("true"))
					component.put(child.getName(), true);
				else if (value.equals("false"))
					component.put(child.getName(), false);
				else
					try {
						component.put(child.getName(),
								Double.parseDouble(value));
						component.put(child.getName(), Integer.parseInt(value));
					} catch (NumberFormatException e) {
						component.put(child.getName(), value);
					}
			}
		}
	}

	public static void generateUI(Element element, JSONObject component)
			throws JSONException, JDOMException {
		Namespace ns = element.getNamespace();
		Attribute typeAt;
		if ((typeAt = element.getAttribute("type")) != null) {
			component.put("name", element.getAttributeValue("name"));
			String label = getDocumentation(element);
			if ((label == null) || (label.equals("")))
				label = element.getAttributeValue("name");
			component.put("fieldLabel", label);

			String type = typeAt.getValue();
			setFieldType(component, type);
			getAppinfo(element, component);
		} else {
			Element typeEl;
			JSONArray items;
			Iterator it;
			Iterator iter;
			if ((typeEl = element.getChild("complexType", ns)) != null) {
				component.put("layout", "form");
				component.put("autoHeight", true);
				component.put("defaults", new JSONObject("{anchor: '100%'}"));
				component.put("tag", element.getName());
				component.put("name", element.getAttributeValue("name"));
				getAppinfo(element, component);

				items = new JSONArray();
				component.put("items", items);
				List children = typeEl.getChildren();
				for (it = children.iterator(); it.hasNext();) {
					Element child = (Element) it.next();
					String childName = child.getName();
					if (childName.equals("attribute")) {
						JSONObject comp = new JSONObject();
						comp.put("tag", childName);
						comp.put("name", child.getAttributeValue("name"));
						comp.put("stateful", false);
						String label = getDocumentation(child);
						if ((label == null) || (label.equals("")))
							label = child.getAttributeValue("name");
						comp.put("fieldLabel", label);

						setFieldType(comp, child.getAttributeValue("type", ""));
						getAppinfo(child, comp);

						XPath xpath = XPath
								.newInstance("ns:simpleType/ns:restriction/ns:enumeration[not(@used)]");
						xpath.addNamespace("ns", ns.getURI());
						Element enumEl = (Element) xpath
								.selectSingleNode(child);
						if (enumEl != null) {
							comp.put("value", enumEl.getAttributeValue("value"));
							comp.put("title", getDocumentation(enumEl));
							comp.put("readOnly", true);
							comp.put("stateful", false);
							enumEl.setAttribute("used", "true");
						}
						items.put(comp);
					} else {
						if ((childName.equals("sequence"))
								|| (childName.equals("all"))) {
							List list = child.getChildren();
							for (iter = list.iterator(); iter.hasNext();) {
								Element el = (Element) iter.next();
								int minOccurs = Integer.parseInt(el
										.getAttributeValue("minOccurs", "1"));
								int i = 0;
								do {
									JSONObject comp = new JSONObject();
									comp.put("tag", el.getName());
									comp.put("name",
											el.getAttributeValue("name"));
									comp.put("stateful", false);
									items.put(comp);
									generateUI(el, comp);
								} while (++i < minOccurs);
							}
						} else if (!(childName.equals("choice"))) {
							if (childName.equals("group")) {
								component.put("xtype", "fieldset");
								component.put("border", true);
								List list = child.getChildren();
								for (iter = list.iterator(); iter.hasNext();) {
									Element el = (Element) iter.next();
									JSONObject comp = new JSONObject();
									comp.put("tag", el.getName());
									comp.put("name",
											el.getAttributeValue("name"));
									generateUI(el, comp);
									items.put(comp);
								}
							}
						}
					}
				}
			} else if ((typeEl = element.getChild("simpleType",
					element.getNamespace())) != null) {
				component.put("name", element.getAttributeValue("name"));
				component.put("tag", element.getName());
				String label = getDocumentation(element);
				if ((label == null) || (label.equals("")))
					label = element.getAttributeValue("name");
				component.put("fieldLabel", label);
				Element restrictionEl;
				if ((restrictionEl = typeEl.getChild("restriction", ns)) != null) {
					List list = restrictionEl.getChildren("enumeration", ns);
					if (list.size() > 0) {
						JSONArray array = new JSONArray();
						for (iter = list.iterator(); iter.hasNext();) {
							Element enumEl = (Element) iter.next();
							String enumText = getDocumentation(enumEl);
							if ((enumText == null) || (enumText.equals("")))
								enumText = enumEl.getAttributeValue("value");
							JSONArray item = new JSONArray();
							item.put(enumEl.getAttributeValue("value"));
							item.put(enumText);
							array.put(item);
						}
						component.put("xtype", "combo");
						component.put("store", array);
						component.put("forceSelection", true);
						component.put("allowBlank", false);
						component.put("triggerAction", "all");
					} else {
						String type = restrictionEl.getAttributeValue("base",
								"");
						setFieldType(component, type);
						Element patternEl = restrictionEl.getChild("pattern",
								ns);
						if (patternEl != null) {
							component.put("regex",
									patternEl.getAttributeValue("value"));
							component.put("regexText",
									getDocumentation(patternEl));
						}
					}
				} else {
					Element listEl;
					if ((listEl = typeEl.getChild("list", ns)) != null) {
						component.put("xtype", "textfield");
					} else {
						Element unionEl;
						if ((unionEl = typeEl.getChild("union", ns)) != null) {
							component.put("xtype", "textfield");
						}
					}
				}
				getAppinfo(element, component);
			} else {
				component.put("style", "display:none;");
			}
		}
	}

	static void setFieldType(JSONObject field, String type)
			throws JSONException {
		if ((type.equals("int")) || (type.equals("integer"))
				|| (type.equals("short")) || (type.equals("long"))) {
			field.put("xtype", "numberfield");
			field.put("allowDecimals", false);
		} else if (type.startsWith("unsigned")) {
			field.put("xtype", "numberfield");
			field.put("allowDecimals", false);
			field.put("allowNegative", false);
		} else if (type.equals("float")) {
			field.put("xtype", "numberfield");
		} else {
			field.put("xtype", "textfield");
		}
	}

	public static Element schema2Xml(XmlSchema schema, String rootname,
			Element flowElement) throws Exception {
		XmlSchemaElement schemaEl = schema.getElementByName(rootname);
		if (schemaEl == null) {
			throw new Exception("schemaElement [" + rootname
					+ "] doesn't exist in the schema!!");
		}
		XmlSchemaType xsdType = schemaEl.getSchemaType();
		String rootNamespaceURI = schemaEl.getQName().getNamespaceURI();
		xmlRootElement = new Element(rootname, rootNamespaceURI);

		generate(xsdType, schema, xmlRootElement);
		Iterator it;
		if (flowElement != null) {
			List nsList = flowElement.getAdditionalNamespaces();
			for (it = nsList.iterator(); it.hasNext();) {
				Object nsObj = it.next();
				Namespace ns = (Namespace) nsObj;
				xmlRootElement.addNamespaceDeclaration(ns);
			}
		}
		return xmlRootElement;
	}

	static void generate(XmlSchemaType xsdType, XmlSchema schema,
			Element xmlRoot) throws Exception {
		Iterator elements = getElements(xsdType);
		while (elements.hasNext()) {
			XmlSchemaElement element = (XmlSchemaElement) elements.next();
			String elementName = element.getName();
			Element ele;
			if (element.getRefName() != null) {
				Namespace childNS = Namespace.getNamespace(element.getRefName()
						.getPrefix(), element.getRefName().getNamespaceURI());
				xmlRootElement.addNamespaceDeclaration(childNS);
				namespaceMap.put(childNS.getURI(), childNS.getPrefix());
				if (element.getQName() == null) {
					ele = new Element(elementName, element.getRefName()
							.getPrefix(), element.getRefName()
							.getNamespaceURI());
				} else {
					String prifix = (String) namespaceMap.get(element
							.getQName().getNamespaceURI());
					if (prifix == null)
						ele = new Element(elementName, element.getRefName()
								.getPrefix(), element.getQName()
								.getNamespaceURI());
					else
						ele = new Element(elementName, prifix, element
								.getQName().getNamespaceURI());
				}
			} else {
				if (element.getQName() == null) {
					ele = new Element(elementName,
							xmlRoot.getNamespacePrefix(),
							xmlRoot.getNamespaceURI());
				} else {
					String prifix = (String) namespaceMap.get(element
							.getQName().getNamespaceURI());
					if (prifix == null)
						ele = new Element(elementName,
								xmlRoot.getNamespacePrefix(), element
										.getQName().getNamespaceURI());
					else {
						ele = new Element(elementName, prifix, element
								.getQName().getNamespaceURI());
					}
				}
			}
			XmlSchemaType xsType = (element.getRefName() != null) ? schema
					.getElementByName(element.getRefName()).getSchemaType()
					: element.getSchemaType();

			if (element.getRefName() != null)
				element = schema.getElementByName(element.getRefName());
			if (xsType instanceof XmlSchemaSimpleType) {
				ele.addContent("?");
				xmlRoot.addContent(ele);
			} else if (element.getSchemaType() instanceof XmlSchemaComplexType) {
				xmlRoot.addContent(ele);
				generate(element.getSchemaType(), schema, ele);
			}
		}
	}

	static Iterator<?> getElements(XmlSchemaType schemaType) throws Exception {
		XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
		if (!(complexType.getParticle() instanceof XmlSchemaSequence)) {
			throw new Exception("can not find the child element sequence!");
		}
		XmlSchemaSequence xsdSequence = (XmlSchemaSequence) complexType
				.getParticle();

		XmlSchemaObjectCollection coll = xsdSequence.getItems();
		Iterator elements = coll.getIterator();
		return elements;
	}
}