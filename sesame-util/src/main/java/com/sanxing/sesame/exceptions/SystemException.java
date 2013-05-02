package com.sanxing.sesame.exceptions;

public class SystemException extends RuntimeException implements KeyedErr {
	private static final long serialVersionUID = 1371868851426481962L;
	private String errorCode = "99999";

	private String moduleName = "99";
	private String[] errMsgArgs;

	public static void registerErrMsgs(String fileName) {
		ErrMessages.addErrorMsgFile(fileName);
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public String getErrKey() {
		return getGlobalErrCode();
	}

	public String getGlobalErrCode() {
		return this.moduleName + "." + this.errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getModuleName() {
		return this.moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String[] getErrMsgArgs() {
		return this.errMsgArgs;
	}

	public void setErrMsgArgs(String[] errMsgArgs) {
		this.errMsgArgs = errMsgArgs;
	}

	public SystemException() {
	}

	public SystemException(String moduleName, String errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
		this.moduleName = moduleName;
	}

	public SystemException(Throwable cause) {
		super(cause);
	}

	public String getMessage() {
		if (getErrMsgArgs() == null) {
			return ErrMessages.getErrMsg(getModuleName(), getErrorCode());
		}
		return ErrMessages.getErrMsg(getModuleName(), getErrorCode(),
				getErrMsgArgs());
	}
}