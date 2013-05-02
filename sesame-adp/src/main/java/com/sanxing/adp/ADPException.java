package com.sanxing.adp;

import com.sanxing.sesame.exceptions.SystemException;

public class ADPException extends SystemException {
	private static final long serialVersionUID = 5101780486758962830L;

	static {
		registerErrMsgs("ADP_ERR.MSG");
	}

	public ADPException(String errCode, Throwable e) {
		super(e);
		setModuleName("997");
		setErrorCode(errCode);
	}

	public ADPException(String errorCode, String arg, Throwable e) {
		super(e);
		setModuleName("997");
		setErrorCode(errorCode);
		setErrMsgArgs(new String[] { arg });
	}

	public ADPException(String errorCode, String arg) {
		setModuleName("997");
		setErrorCode(errorCode);
		setErrMsgArgs(new String[] { arg });
	}
}