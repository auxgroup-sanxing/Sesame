package com.sanxing.adp.runtime;

import com.sanxing.adp.parser.FaultInfo;
import com.sanxing.adp.parser.OperationInfo;
import com.sanxing.adp.parser.PortTypeInfo;
import com.sanxing.adp.parser.WSDLParser;
import com.sanxing.adp.util.XJUtil;
import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.exceptions.AppException;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import javax.wsdl.Definition;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMResult;

public class ADPServer {
	static Logger LOG = LoggerFactory.getLogger(ADPServer.class);
	JarFileClassLoader jarFileClassLoader;
	BaseMethodProcessor voidBMP = new VoidMethodProcessor();

	BaseMethodProcessor returnBMP = new ReturnMethodProcessor();

	public synchronized void resetClassLoader() {
		URL[] urls = this.jarFileClassLoader.getURLs();
		ClassLoader[] parents = this.jarFileClassLoader.getParents();
		this.jarFileClassLoader.destroy();
		this.jarFileClassLoader = new JarFileClassLoader(urls, parents);
	}

	public ADPServer() {
		LOG.info("starting adp server");
		if (this.jarFileClassLoader == null) {
			this.jarFileClassLoader = new JarFileClassLoader(new URL[0], Thread
					.currentThread().getContextClassLoader());
		}
		this.voidBMP.setServer(this);
		this.returnBMP.setServer(this);
	}

	public ADPServer(JarFileClassLoader classLoader) {
		this.jarFileClassLoader = classLoader;
		this.voidBMP.setServer(this);
		this.returnBMP.setServer(this);
	}

	public synchronized void addClassPath(URL url) {
		this.jarFileClassLoader.addURL(url);
	}

	public synchronized void addJarDirCP(File dir) {
		this.jarFileClassLoader.addJarDir(dir);
	}

	public synchronized void addClassesCP(File dir) {
		this.jarFileClassLoader.addClassesDir(dir);
	}

	public synchronized void register(Definition def) throws Exception {
		WSDLParser parser = new WSDLParser();
		try {
			parser.parse4runTime(def, this.jarFileClassLoader);
		} catch (Exception e) {
			LOG.error("parse wsdl file err", e);
			throw e;
		}
	}

	public Document accept(QName portypeName, String operationName,
			Document request) throws Exception {
		PortTypeInfo portTypeInfo = Registry.getInstance().getPortypeInfo(
				portypeName);

		OperationInfo oper = portTypeInfo.getOperation(operationName);

		Object tx = portTypeInfo.getTx(this.jarFileClassLoader);
		try {
			Element root;
			if (oper.isVoid()) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("the method is void");
				}

				root = this.voidBMP.process(request, oper, tx);
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("the method is not void");
				}

				root = this.returnBMP.process(request, oper, tx);
			}
			return new Document(root);
		} catch (Exception e) {
			Element faultEle = exceptionHandle(oper, e);
			if (faultEle != null) {
				return new Document(faultEle);
			}
			throw e;
		}
	}

	private Element exceptionHandle(OperationInfo oper, Exception e) {
		if (e instanceof AppException) {
			Element root = new Element("fault");
			List<FaultInfo> faults = oper.getFaults();
			LOG.trace("exception name [" + e.getClass().getSimpleName() + "]");
			for (FaultInfo fault : faults) {
				LOG.trace("check fault [" + fault.getName() + "]");
				if (!((fault.getName() + "Exception").equals(e.getClass()
						.getSimpleName())))
					continue;
				try {
					root.addContent(new Element("faultcode")
							.setText(((AppException) e).getGlobalErrCode()));
					root.addContent(new Element("faultstring").setText(e
							.getMessage()));
					root.addContent(new Element("faultactor").setText("adp"));
					Field[] fields = e.getClass().getDeclaredFields();
					for (Field field : fields) {
						Marshaller m = JAXBHelper.getMarshallerByClazz(field
								.getType());
						if (m != null) {
							JDOMResult jdomResult = new JDOMResult();
							m.marshal(field.get(e), jdomResult);
							Element partResult = jdomResult.getDocument()
									.getRootElement();
							partResult.detach();
							root.addContent(partResult);
						} else if (XJUtil
								.isPrimitive(field.getType().getName())) {
							Element fieldEle = new Element(field.getName());
							fieldEle.setText(field.get(e).toString());
							root.addContent(fieldEle);
						}
					}

					return root;
				} catch (Exception e1) {
					LOG.error("handle fault err", e1);
				}
			}
		}

		return null;
	}
}