package org.sanxing.sesame.util.cache;

import org.sanxing.sesame.exceptions.SystemException;

public class NoSuchObjectException extends SystemException {
	private static final long serialVersionUID = -505110997198643331L;
	private String key;

	public NoSuchObjectException(String key) {
		setErrMsgArgs(new String[] { key });
	}
}