package com.sanxing.sesame.logging;

import com.sanxing.sesame.logging.constants.LogStage;

public class FinishRecord extends LogRecord {
	private static final long serialVersionUID = -7519058361408503253L;
	private String action;

	public FinishRecord(long serial) {
		setSerial(serial);
		setStage(LogStage.STAGE_END);
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return this.action;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		buf.append(" serial: " + getSerial());
		if (getServiceName() != null) {
			buf.append(", serviceUnit: '" + getServiceName() + "'");
		}
		if (getAction() != null) {
			buf.append(", action: '" + getAction() + "'");
		}
		buf.append(", status: finished }");
		return buf.toString();
	}
}