package com.sanxing.sesame.engine.action.beanshell.func;

import bsh.CallStack;
import bsh.EvalError;
import bsh.Interpreter;
import com.sanxing.sesame.exceptions.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class debug {
	static Logger LOG = LoggerFactory
			.getLogger("com.sanxing.sesame.engine.action.beanshell");

	public static void invoke(Interpreter env, CallStack callstack,
			String message) throws EvalError {
		try {
			System.out.println(message);
		} catch (Exception e) {
			env.set("error", new SystemException());
		}
	}

	public static void invoke(Interpreter env, CallStack callstack,
			Object message) {
		System.out.println(message.toString());
	}
}