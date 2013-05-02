package com.sanxing.sesame.engine.action.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.exceptions.SystemException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanShellContext {
	static final Logger LOG = LoggerFactory.getLogger(BeanShellContext.class);

	private Interpreter interpreter = new Interpreter();

	private Set<String> functions = new HashSet();

	private Set<String> vars = new HashSet();

	public void addStaticFuncPath(String path) {
		try {
			this.interpreter.eval("importCommands(\"" + path + "\");");
			LOG.debug(" static import command[importCommands(\"" + path
					+ "\");" + "]");
		} catch (EvalError e) {
			e.printStackTrace();
		}
	}

	BeanShellContext() {
		try {
			LOG.debug("static import................");
			this.interpreter
					.eval("importCommands(\"com.sanxing.sesame.engine.action.beanshell.func\");");
			this.interpreter.eval("import org.jdom2.* ;");
		} catch (EvalError e) {
			e.printStackTrace();
		}
	}

	public void registerFunction(String name, String functionScript)
			throws SystemException {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug(this.interpreter + " register function [" + name
						+ "]");
				LOG.debug("function script [" + functionScript + "]");
			}
			this.interpreter.eval(functionScript);

			this.functions.add(name);
		} catch (EvalError e) {
			LOG.error("register function err", e);
			throw new SystemException("998", "00001", e.getCause());
		}
	}

	public boolean isFunctionResitered(String functionName) {
		return (this.functions.contains(functionName));
	}

	public void executeFunction(String functionName) throws AppException {
		try {
			this.interpreter.eval(functionName + "()");
		} catch (TargetError e) {
			ActionException se = new ActionException(e.getTarget());
			se.setModuleName("998");
			se.setErrorCode("00012");
			se.setErrMsgArgs(new String[] { "" + e.getErrorLineNumber() });
			LOG.error("app or system err", se);
			if (e.getTarget() instanceof AppException) {
				LOG.debug("throw app exception");
				throw ((AppException) e.getTarget());
			}
			if (e.getTarget() instanceof SystemException) {
				LOG.debug("throw sys exception");
				throw ((SystemException) e.getTarget());
			}
			throw new SystemException(e);
		} catch (ParseException e) {
			ActionException se = new ActionException(e);
			se.setModuleName("998");
			se.setErrorCode("00012");
			se.setErrMsgArgs(new String[] { "" + e.getErrorLineNumber() });
			LOG.error("beanshell syntax err", se);
		} catch (EvalError e) {
			e.printStackTrace();
			ActionException se = new ActionException(e);
			se.setModuleName("998");
			se.setErrorCode("00012");
			se.setErrMsgArgs(new String[] { "" + e.getErrorLineNumber() });
			LOG.error("unexcpedted beanshell eval err", se);
			throw se;
		}
	}

	public void set(String name, Object value) {
		try {
			this.interpreter.set(name, value);
			this.vars.add(name);
		} catch (EvalError e) {
			e.printStackTrace();
		}
	}

	public void close() throws EvalError {
		String str;
		for (Iterator localIterator = this.vars.iterator(); localIterator
				.hasNext(); str = (String) localIterator.next())
			;
	}
}