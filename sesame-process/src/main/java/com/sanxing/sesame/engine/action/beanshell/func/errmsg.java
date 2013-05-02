package com.sanxing.sesame.engine.action.beanshell.func;

import bsh.CallStack;
import bsh.Interpreter;
import com.sanxing.sesame.exceptions.AppException;

public class errmsg {
	public static void invoke(Interpreter env, CallStack callstack,
			String moduleName, String errCode) throws AppException {
		AppException ae = new AppException(moduleName);
		ae.setErrorCode(errCode);

		throw ae;
	}
}