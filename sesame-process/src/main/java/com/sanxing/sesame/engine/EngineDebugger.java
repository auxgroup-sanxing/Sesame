package com.sanxing.sesame.engine;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.component.params.AppParameters;
import com.sanxing.sesame.component.params.Parameter;
import com.sanxing.sesame.component.params.Parameter.PARAMTYPE;
import com.sanxing.sesame.engine.component.ProcessEngine;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.xpath.XPathUtil;
import com.sanxing.sesame.engine.xslt.TransformerManager;
import com.sanxing.sesame.core.naming.JNDIUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jaxen.Function;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.MDC;

public class EngineDebugger {
	private static Logger LOG = LoggerFactory.getLogger(EngineDebugger.class);

	private Map<String, ClassLoader> loaders = new HashMap();
	private ExecutionContext executionCtx;
	private Thread thread;

	public EngineDebugger(ExecutionContext executionCtx) {
		this.executionCtx = executionCtx;

		Object serial = executionCtx.get("process.serial");

		this.executionCtx.getDataContext()
				.addVariable(
						"serial",
						new Variable((serial != null) ? serial : Integer
								.valueOf(0), 8));

		this.executionCtx.put("NAMING_CONTEXT", JNDIUtil.getInitialContext());

		this.executionCtx.put("ENGINE", ProcessEngine.jbiInstance);

		appendBizParameters(executionCtx);
	}

	private void appendBizParameters(ExecutionContext executionCtx) {
		String suName = (String) this.executionCtx.get("service_name");
		String operationName = (String) this.executionCtx.get("operation_name");

		LOG.debug("append biz param : suName [" + suName + "]");

		LOG.debug("append biz param : operationName [" + operationName + "]");

		List<String> paramNames = new LinkedList();
		paramNames.addAll(AppParameters.getInstance().getAppParamKeys());
		paramNames.addAll(AppParameters.getInstance().getSuParamKeys(suName));
		paramNames.addAll(AppParameters.getInstance().getOperationParamKeys(
				suName, operationName));
		for (String paramName : paramNames) {
			Parameter param = AppParameters.getInstance().getParamter(suName,
					operationName, paramName);
			LOG.debug("APPEND BIZ PARAMEER [" + param + "]");
			if (param.getType().equals(Parameter.PARAMTYPE.PARAM_TYPE_BOOLEAN))
				executionCtx.getDataContext().addVariable(param.getName(),
						new Variable(param.getTypedValue(), 6));
			else if ((param.getType()
					.equals(Parameter.PARAMTYPE.PARAM_TYPE_INT))
					|| (param.getType()
							.equals(Parameter.PARAMTYPE.PARAM_TYPE_DOUBLE)))
				executionCtx.getDataContext().addVariable(param.getName(),
						new Variable(param.getTypedValue(), 8));
			else if (param.getType().equals(
					Parameter.PARAMTYPE.PARAM_TYPE_STRING))
				executionCtx.getDataContext().addVariable(param.getName(),
						new Variable(param.getTypedValue(), 7));
		}
	}

	public void start(String componentRoot, final String flowName) {
		ClassLoader classloader = (ClassLoader) this.loaders.get(componentRoot);
		if (classloader == null) {
			JarFileClassLoader loader = new JarFileClassLoader(new URL[0],
					Thread.currentThread().getContextClassLoader(), false,
					new String[0], new String[] { "java.", "javax." });
			File folder = new File(componentRoot);
			loader.addClassesDir(folder);
			loader.addJarDir(new File(folder, "lib"));
			this.loaders.put(componentRoot, loader);

			registerTransformExtension(componentRoot, loader);
			registerCustomXPathFunction(componentRoot, loader);

			classloader = loader;
		}

		this.executionCtx.put("process.classloader", classloader);
		this.executionCtx.openDebugging();
		Runnable worker = new Runnable() {
			public void run() {
				try {
					TransformerManager.clearCache(flowName);
					MDC.put("ACTION", "debugger");
					if (EngineDebugger.this.executionCtx.get("process.serial") != null) {
						MDC.put("SERIAL", String
								.valueOf(EngineDebugger.this.executionCtx
										.get("process.serial")));
					}
					MDC.put("CLIENT_TYPE", "debugger");
					MDC.put("CLIENT_ID", "debugger");
					if (EngineDebugger.this.executionCtx.getUuid() != null) {
						MDC.put("CLIENT_SERIAL",
								EngineDebugger.this.executionCtx.getUuid());
					}
					EngineDebugger.this.executionCtx.put("process.ACTION",
							flowName);
					Engine.getInstance().execute(
							EngineDebugger.this.executionCtx, flowName);
				} catch (Throwable t) {
					EngineDebugger.LOG.error(t.getMessage(), t);
					try {
						EngineDebugger.this.executionCtx.put("exception", t);
						EngineDebugger.this.executionCtx
								.setCurrentAction("exception");
					} catch (InterruptedException localInterruptedException) {
					}
				} finally {
					if ((EngineDebugger.this.executionCtx != null)
							&& (!(EngineDebugger.this.executionCtx
									.isDehydrated())))
						EngineDebugger.this.executionCtx.closeDebugging();
				}
			}
		};
		this.thread = new Thread(worker);
		this.thread.setContextClassLoader(classloader);
		this.thread.setDaemon(true);
		this.thread.start();
	}

	public void resume() {
		synchronized (this.executionCtx) {
			this.executionCtx.closeDebugging();
			this.executionCtx.notify();
		}
	}

	public void terminate() {
		synchronized (this.executionCtx) {
			this.executionCtx.closeDebugging();
			this.executionCtx.terminate();
			this.executionCtx.notify();
		}
	}

	public void nextStep() throws InterruptedException {
		synchronized (this.executionCtx) {
			this.executionCtx.notify();
		}
	}

	public DataContext getCurrentContext() {
		return this.executionCtx.getDataContext();
	}

	private void registerTransformExtension(String componentRoot,
			ClassLoader classloader) {
		File extensionFile = new File(componentRoot, "transform.ext");
		if (extensionFile.exists()) {
			SAXBuilder builder = new SAXBuilder();
			try {
				Document doc = builder.build(extensionFile);
				List<Element> list = doc.getRootElement().getChildren("class");
				for (Element classEl : list) {
					String prefix = classEl.getAttributeValue("prefix");
					String className = classEl.getAttributeValue("class-name");
					classloader.loadClass(className);
					LOG.debug("register class [" + prefix + ":" + className
							+ "]");
					TransformerManager.registerExtension(prefix, className);
				}
			} catch (JDOMException e) {
				throw new RuntimeException("please check transform.ext", e);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	private void registerCustomXPathFunction(String componentRoot,
			ClassLoader classloader) {
		File functionFile = new File(componentRoot, "xpath.ext");
		if (!(functionFile.exists()))
			return;
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(functionFile);
			List fuctionEles = doc.getRootElement().getChildren();
			for (int i = 0; i < fuctionEles.size(); ++i) {
				String name = null;
				String namespaceUri = null;
				try {
					Element eleFunc = (Element) fuctionEles.get(i);
					String prefix = eleFunc.getAttributeValue("prefix");
					name = eleFunc.getAttributeValue("name");
					namespaceUri = "http://www.sanxing.net.cn/sesame/"
							+ prefix;
					String className = eleFunc.getAttributeValue("class-name");
					String desc = eleFunc.getAttributeValue("description");
					LOG.debug("add function [" + prefix + ":" + namespaceUri
							+ "]" + name);
					Class functionClazz = classloader.loadClass(className);
					Function function = (Function) functionClazz.newInstance();
					XPathUtil.registerFunction(namespaceUri, prefix, name,
							function);
				} catch (Exception e) {
					LOG.error("Register function [" + namespaceUri + ":" + name
							+ "] err", e);
				}
			}
		} catch (JDOMException e) {
			throw new RuntimeException("please check function.xml", e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}