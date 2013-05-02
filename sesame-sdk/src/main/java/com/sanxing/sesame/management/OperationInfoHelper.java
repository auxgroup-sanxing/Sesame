package com.sanxing.sesame.management;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class OperationInfoHelper {
	private List<MBeanOperationInfo> list = new ArrayList();

	public ParameterHelper addOperation(Object theObject, String name,
			String description) {
		return addOperation(theObject, name, 0, description);
	}

	public ParameterHelper addOperation(Object theObject, String name,
			int numberParams, String description) {
		Method method = getMethod(theObject.getClass(), name, numberParams);
		MBeanOperationInfo opInfo = new MBeanOperationInfo(description, method);
		this.list.add(opInfo);
		MBeanParameterInfo[] result = opInfo.getSignature();
		return new ParameterHelper(result);
	}

	public MBeanOperationInfo[] getOperationInfos() {
		MBeanOperationInfo[] result = new MBeanOperationInfo[this.list.size()];
		this.list.toArray(result);
		return result;
	}

	public void clear() {
		this.list.clear();
	}

	public static MBeanOperationInfo[] join(MBeanOperationInfo[] ops1,
			MBeanOperationInfo[] ops2) {
		MBeanOperationInfo[] result = (MBeanOperationInfo[]) null;
		int length = 0;
		int startPos = 0;
		if (ops1 != null) {
			length = ops1.length;
		}
		if (ops2 != null) {
			length += ops2.length;
		}
		result = new MBeanOperationInfo[length];
		if (ops1 != null) {
			System.arraycopy(ops1, 0, result, startPos, ops1.length);
			startPos = ops1.length;
		}
		if (ops2 != null) {
			System.arraycopy(ops2, 0, result, startPos, ops2.length);
		}
		return result;
	}

	private Method getMethod(Class<? extends Object> theClass, String name,
			int numParams) {
		Method result = null;
		Method[] methods = theClass.getMethods();
		if (methods != null) {
			for (int i = 0; i < methods.length; ++i) {
				if ((methods[i].getName().equals(name))
						&& (methods[i].getParameterTypes().length == numParams)) {
					result = methods[i];
					break;
				}
			}
			if (result == null) {
				for (int i = 0; i < methods.length; ++i) {
					if (methods[i].getName().equals(name)) {
						result = methods[i];
						break;
					}
				}
			}
		}
		return result;
	}
}