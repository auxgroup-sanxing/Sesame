package com.sanxing.adp.runtime;

import com.sanxing.adp.ADPException;
import com.sanxing.adp.api.ResultHolder;
import com.sanxing.adp.parser.OperationInfo;
import com.sanxing.adp.parser.PartInfo;
import com.sanxing.adp.util.XJUtil;
import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.util.JdomUtil;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;

public abstract class BaseMethodProcessor {
	ADPServer server;
	private static Logger LOG = LoggerFactory.getLogger(BaseMethodProcessor.class);

	public abstract Element process(Document paramDocument,
			OperationInfo paramOperationInfo, Object paramObject)
			throws ADPException, AppException;

	public void setServer(ADPServer server) {
		this.server = server;
	}

	void fufillINParams(OperationInfo oper, Element body, Object[] paramObjets)
			throws ADPException {
		int i = 0;

		if (body == null) {
			throw new ADPException("00006", oper.getCapOperationName());
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("prepared to fufill input parameters...............");
			LOG.debug("input document is ...........\n "
					+ JdomUtil.print(new JDOMSource(body)));
		}
		List<PartInfo> params = oper.getParams();
		for (PartInfo parameterInfo : params)
			try {
				QName elementName = parameterInfo.getElementName();
				if (LOG.isDebugEnabled()) {
					LOG.debug("parsing element [" + elementName + "]");
				}
				String javaType = parameterInfo.getJavaType();
				Element part = null;
				if (elementName != null) {
					Namespace ns = Namespace.getNamespace(
							elementName.getPrefix(),
							elementName.getNamespaceURI());
					part = body.getChild(elementName.getLocalPart(), ns);
					if (part != null) {
						part.setNamespace(Namespace.getNamespace("",
								elementName.getNamespaceURI()));
						break;
					}
					throw new ADPException("00007", elementName.getLocalPart());
				}
				part = body.getChild(parameterInfo.getName());

				if (XJUtil.isPrimitive(javaType)) {
					String param = part.getText();
					Object paramObj = XJUtil.xmlPrimitiv2Java(
							parameterInfo.getJavaType(),
							parameterInfo.getXsType(), param);
					paramObjets[i] = paramObj;
				} else {
					Unmarshaller unmarshaller = JAXBHelper
							.getUnMarshallerByClazz(this.server.jarFileClassLoader
									.loadClass(javaType));

					Object paramObject = unmarshaller.unmarshal(new JDOMSource(
							part));
					paramObjets[i] = paramObject;
				}

				if (LOG.isDebugEnabled()) {
					LOG.debug("parsed element [" + elementName + "]");
				}

				++i;
			} catch (JAXBException e) {
				throw new ADPException("00005", e);
			} catch (Exception e) {
				throw new ADPException("99999", e);
			}
	}

	void allAddtionNamespace(Element part, String NamespaceURI) {
		List childrens = part.getChildren();
		for (int i = 0; i < childrens.size(); ++i) {
			Element addition = (Element) childrens.get(i);
			addition.setNamespace(Namespace.getNamespace("", NamespaceURI));
			if (addition.getChildren().size() > 0)
				allAddtionNamespace(addition, NamespaceURI);
		}
	}

	void fufillOUTParams(OperationInfo oper, Element body, Object[] paramObjets) {
		int i = oper.getMethodParamCount() - oper.getParams().size() + 1;
		for (PartInfo parameterInfo : oper.getResults()) {
			ResultHolder holder = new ResultHolder();
			paramObjets[i] = holder;
			++i;
		}
	}
}