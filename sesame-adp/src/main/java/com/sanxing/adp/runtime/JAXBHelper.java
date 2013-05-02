package com.sanxing.adp.runtime;

import com.sanxing.adp.util.XJUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBHelper {
	static Logger LOG = LoggerFactory.getLogger(JAXBHelper.class);

	private static ThreadLocal<Map<Class, Marshaller>> marshallersInThread = new ThreadLocal();

	private static Map<String, JAXBContext> JaxbContextCache = new ConcurrentHashMap();

	private static ThreadLocal<Map<Class, Unmarshaller>> unmarshallersInThread = new ThreadLocal();

	public static void reset() {
		synchronized (JAXBHelper.class) {
			JaxbContextCache.clear();
		}
	}

	private static Map<Class, Marshaller> getMarshallersInThread() {
		if (marshallersInThread.get() == null) {
			marshallersInThread.set(new HashMap());
		}
		return ((Map) marshallersInThread.get());
	}

	public static Marshaller getMarshallerByClazz(Class clazz) {
		if (!(getMarshallersInThread().containsKey(clazz))) {
			getMarshallersInThread().put(clazz, makeMarshaller(clazz));
		}
		return ((Marshaller) getMarshallersInThread().get(clazz));
	}

	private static Marshaller makeMarshaller(Class clazz) {
		if (!(XJUtil.isPrimitive(clazz.getName()))) {
			try {
				String packageName = clazz.getPackage().getName();
				JAXBContext jc = getJAXBContext(clazz);
				Marshaller marshaller = jc.createMarshaller();
				return marshaller;
			} catch (Exception e) {
				LOG.error("make marshaller err", e);
				throw new RuntimeException("make marshaller err", e);
			}
		}
		throw new RuntimeException("is premitive type, no marshaller");
	}

	private static JAXBContext getJAXBContext(Class clazz) throws JAXBException {
		String packageName = clazz.getPackage().getName();
		if (!(JaxbContextCache.containsKey(packageName))) {
			JAXBContext jc = JAXBContext.newInstance(packageName,
					clazz.getClassLoader());
			JaxbContextCache.put(packageName, jc);
		}
		return ((JAXBContext) JaxbContextCache.get(packageName));
	}

	private static Map<Class, Unmarshaller> getUnmarshallersInThread() {
		if (unmarshallersInThread.get() == null) {
			unmarshallersInThread.set(new HashMap());
		}
		return ((Map) unmarshallersInThread.get());
	}

	public static Unmarshaller getUnMarshallerByClazz(Class clazz) {
		if (!(getUnmarshallersInThread().containsKey(clazz))) {
			getUnmarshallersInThread().put(clazz, makeUnMarshaller(clazz));
		}
		return ((Unmarshaller) getUnmarshallersInThread().get(clazz));
	}

	private static Unmarshaller makeUnMarshaller(Class clazz) {
		if (!(XJUtil.isPrimitive(clazz.getName()))) {
			try {
				JAXBContext jc = getJAXBContext(clazz);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				return unmarshaller;
			} catch (Exception e) {
				LOG.error("make unmarshaller err", e);
				throw new RuntimeException("make unmarshaller err", e);
			}
		}
		throw new RuntimeException("is premitive type, no unmarshaller");
	}
}