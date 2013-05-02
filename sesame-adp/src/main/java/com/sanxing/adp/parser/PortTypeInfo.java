package com.sanxing.adp.parser;

import com.sanxing.adp.util.XJUtil;
import com.sanxing.sesame.util.GetterUtil;
import com.sanxing.sesame.util.SystemProperties;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortTypeInfo {
	private QName definationName;
	private QName name;
	private List<OperationInfo> operations = new LinkedList();

	private static Logger LOG = LoggerFactory.getLogger(PortTypeInfo.class);
	private String className;
	Map<Class, Object> beanCache = new ConcurrentHashMap();

	public void addOperation(OperationInfo operationInfo) {
		this.operations.add(operationInfo);
	}

	public List<OperationInfo> getOperations() {
		return this.operations;
	}

	public QName getName() {
		return this.name;
	}

	public void setName(QName name) {
		this.name = name;
	}

	public QName getDefinationName() {
		return this.definationName;
	}

	public void setDefinationName(QName definationName) {
		this.definationName = definationName;
	}

	public OperationInfo getOperation(String operQName) {
		for (OperationInfo info : this.operations) {
			if (info.getOperationName().equals(operQName)) {
				return info;
			}
		}
		throw new RuntimeException("unkown operation :[" + operQName + "]");
	}

	public String getClassName() {
		if (this.className == null) {
			this.className = XJUtil.ns2ClassName(getName()) + "Impl";
		}
		return this.className;
	}

	public Object getTx() {
		try {
			Class clazz = PortTypeInfo.class.getClassLoader().loadClass(
					getClassName());

			Object tx = clazz.newInstance();
			return tx;
		} catch (InstantiationException e) {
			LOG.error("initialize class [" + getClassName() + "] err", e);
		} catch (IllegalAccessException e) {
			LOG.error("initialize class [" + getClassName() + "] err", e);
		} catch (ClassNotFoundException e) {
			LOG.error("initialize class [" + getClassName() + "] err", e);
		}
		return null;
	}

	public Object getTx(ClassLoader load) {
		try {
			boolean statefulADPBean = GetterUtil.getBoolean(
					SystemProperties.get("com.sanxing.sesame.adp.stateful"),
					false);
			if (statefulADPBean) {
				Class clazz = load.loadClass(getClassName());
				Object tx = clazz.newInstance();
				return tx;
			}
			Class clazz = load.loadClass(getClassName());
			if (!(this.beanCache.containsKey(clazz))) {
				Object tx = clazz.newInstance();
				this.beanCache.put(clazz, tx);
			}
			return this.beanCache.get(clazz);
		} catch (Exception e) {
			throw new RuntimeException("initialize class [" + getClassName()
					+ "] err,please check class path ", e);
		}
	}

	public String toString() {
		return "PortTypeInfo{definationName=" + this.definationName + ", name="
				+ this.name + ", operations=" + this.operations + '}';
	}
}