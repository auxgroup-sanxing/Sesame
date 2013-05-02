package com.sanxing.sesame.engine.action.callout;

import com.sanxing.sesame.exceptions.SystemException;

public class CalloutException extends SystemException {
	private static final long serialVersionUID = 6693352555619802900L;
	private String message;

	public CalloutException(String key, String message) {
		this(key, message, null);
	}

	public CalloutException(String key, String message, Throwable cause) {
		super(cause);
		int p = (key != null) ? key.indexOf(46) : -1;
		setModuleName((p >= 0) ? key.substring(0, p) : "call");
		setErrorCode((p >= 0) ? key.substring(p + 1) : key);
		this.message = message;
	}

	public String getKey() {
		return getGlobalErrCode();
	}

	public String getMessage() {
		if (this.message != null) {
			return this.message;
		}

		return super.getMessage();
	}
}