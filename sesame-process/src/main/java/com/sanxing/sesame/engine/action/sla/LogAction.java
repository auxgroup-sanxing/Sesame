package com.sanxing.sesame.engine.action.sla;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

public class LogAction extends AbstractAction implements Constant {
	private String loggerName;
	private String level = "debug";

	private String varName = "body";

	private String msg = "";
	private String xPath;
	static Logger log;

	public void doinit(Element config) {
		this.loggerName = config.getAttributeValue("name");
		this.level = config.getAttributeValue("level", "debug");
		this.varName = config.getAttributeValue("var", "anonymous");
		this.msg = config.getAttributeValue("msg", "").replace("*", "");
		this.xPath = config.getChildTextTrim("xpath", null);
		if (this.loggerName != null)
			log = LoggerFactory.getLogger(this.loggerName);
		else
			log = LoggerFactory.getLogger(super.getClass());
	}

	public void dowork(DataContext dataContext) {
		String toBeDebug = "\n-----------------------------------\n";
		try {
			String[] logMSG = this.msg.split("[{*}]");
			for (int i = 0; i < logMSG.length; ++i) {
				if ((this.msg.indexOf("{" + logMSG[i] + "}", 0) > 0)
						|| (this.msg.startsWith("{" + logMSG[i] + "}"))) {
					toBeDebug = toBeDebug
							+ getVariable(dataContext, "request", logMSG[i]);
				} else {
					toBeDebug = toBeDebug + logMSG[i].replace("", "*");
				}
			}
			toBeDebug = toBeDebug + "\n"
					+ "-----------------------------------";

			Long serial = (Long) dataContext.getExecutionContext().get(
					"process.serial");

			if (serial != null) {
				toBeDebug = "{serial:" + serial + "}" + toBeDebug;
			}

			Log logger = LogFactory.getLog("sesame.application");
			if (logger != null) {
				if (this.level.equals("debug")) {
					logger.debug(toBeDebug);
				} else if (this.level.equalsIgnoreCase("info")) {
					logger.info(toBeDebug);
				} else if (this.level.equalsIgnoreCase("warn")) {
					logger.warn(toBeDebug);
				} else if (this.level.equalsIgnoreCase("error")) {
					logger.error(toBeDebug);
				} else if (this.level.equalsIgnoreCase("fatal")) {
					logger.fatal(toBeDebug);
				}
				return;
			}

			if (this.level.equals("debug")) {
				log.debug(toBeDebug);
				return;
			}
			if (this.level.equalsIgnoreCase("warn")) {
				log.warn(toBeDebug);
				return;
			}
			if (this.level.equalsIgnoreCase("error")) {
				log.error(toBeDebug);
				return;
			}
			if (this.level.equalsIgnoreCase("fatal")) {
				log.error(toBeDebug);
				return;
			}
			if (this.level.equalsIgnoreCase("info"))
				log.info(toBeDebug);
		} catch (ActionException e) {
			log.error(e.getMessage());

			throw e;
		}
	}

	public static void main(String[] args) {
		String log1 = "asdas.{aa}*0*{s.s}a'a.'a'a";
		String log = log1.replace("*", "");

		String[] xpath = log.split("[{*}]");
		for (int i = 0; i < xpath.length; ++i)
			if ((log.indexOf("{" + xpath[i] + "}", 0) > 0)
					|| (log.startsWith("{" + xpath[i] + "}")))
				System.out.println(xpath[i] + " is var ....");
			else
				System.out.println(xpath[i].replace("", "*") + " isn't vat!!!");
	}
}