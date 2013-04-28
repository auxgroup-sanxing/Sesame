package org.sanxing.sesame.exceptions;

public class AppException extends Exception implements KeyedErr {
	private static final long serialVersionUID = 7471158772918497280L;
	private String moduleName;
	private String errorCode;
	private String[] errMsgArgs;

	public String getErrKey() {
		return getGlobalErrCode();
	}

	public static void registerErrMsgs(String fileName) {
		ErrMessages.addErrorMsgFile(fileName);
	}

	public AppException(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return this.moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String[] getErrMsgArgs() {
		return this.errMsgArgs;
	}

	public void setErrMsgArgs(String[] errArgs) {
		this.errMsgArgs = errArgs;
	}

	public String getGlobalErrCode() {
		return this.moduleName + "." + this.errorCode;
	}

	public String getMessage() {
		if (getErrMsgArgs() == null) {
			return ErrMessages.getErrMsg(getModuleName(), getErrorCode());
		}
		return ErrMessages.getErrMsg(getModuleName(), getErrorCode(),
				getErrMsgArgs());
	}
}