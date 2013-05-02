package com.sanxing.sesame.engine;

public class TrackingInfo {
	public static final int STATUS_OK = 0;
	public static final int STATUS_ERROR = -1;
	private String uuid;
	private String action;
	private String parentAction;
	private String context;
	private int status;
	private String message;
	private long stamp;

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getParentAction() {
		return this.parentAction;
	}

	public void setParentAction(String parentAction) {
		this.parentAction = parentAction;
	}

	public String getContext() {
		return this.context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getStamp() {
		return this.stamp;
	}

	public void setStamp(long stamp) {
		this.stamp = stamp;
	}
}