package com.sanxing.sesame.management;

import javax.management.MBeanParameterInfo;

public class ParameterHelper {
	private MBeanParameterInfo[] infos;

	ParameterHelper(MBeanParameterInfo[] infos) {
		this.infos = infos;
	}

	public void setDescription(int index, String name, String description) {
		MBeanParameterInfo old = this.infos[index];
		this.infos[index] = new MBeanParameterInfo(name, old.getType(),
				description);
	}
}