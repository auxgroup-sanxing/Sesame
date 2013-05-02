package com.sanxing.sesame.engine.action;

import com.sanxing.sesame.engine.action.beanshell.BeanShellAction;
import com.sanxing.sesame.engine.action.flow.BreakAction;
import com.sanxing.sesame.engine.action.flow.DecisionAction;
import com.sanxing.sesame.engine.action.flow.DoWhileAction;
import com.sanxing.sesame.engine.action.flow.FinishAction;
import com.sanxing.sesame.engine.action.flow.ForEachAction;
import com.sanxing.sesame.engine.action.flow.ForkAction;
import com.sanxing.sesame.engine.action.flow.GroupAction;
import com.sanxing.sesame.engine.action.flow.KillTimeAction;
import com.sanxing.sesame.engine.action.flow.WhileDoAction;
import com.sanxing.sesame.engine.action.flow.exceptions.ThrowAction;
import com.sanxing.sesame.engine.action.flow.exceptions.TryCatchAction;
import com.sanxing.sesame.engine.action.jdbc.DBAction;
import com.sanxing.sesame.engine.action.jdbc.TXAction;
import com.sanxing.sesame.engine.action.sla.LogAction;
import com.sanxing.sesame.engine.action.transform.XslTransformAction;
import com.sanxing.sesame.engine.action.var.AppendAction;
import com.sanxing.sesame.engine.action.var.AssignAction;
import com.sanxing.sesame.engine.action.var.CloneAction;
import com.sanxing.sesame.engine.action.var.DeleteAction;
import com.sanxing.sesame.engine.action.var.NamespaceAction;
import com.sanxing.sesame.engine.action.var.RemoveNSAction;
import com.sanxing.sesame.engine.action.var.RenameAction;
import com.sanxing.sesame.engine.component.CalloutAction;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;

public class ActionFactory {
	static Map<String, Class<?>> actionNameMap = new HashMap();

	static Map<Element, Action> actionInstances = new HashMap();

	static Logger logger = LoggerFactory.getLogger(ActionFactory.class);

	static {
		actionNameMap.put("append", AppendAction.class);
		actionNameMap.put("assign", AssignAction.class);
		actionNameMap.put("clone", CloneAction.class);
		actionNameMap.put("delete", DeleteAction.class);
		actionNameMap.put("rename", RenameAction.class);
		actionNameMap.put("namespace", NamespaceAction.class);

		actionNameMap.put("removeNS", RemoveNSAction.class);

		actionNameMap.put("decision", DecisionAction.class);
		actionNameMap.put("do-while", DoWhileAction.class);
		actionNameMap.put("for-each", ForEachAction.class);

		actionNameMap.put("group", GroupAction.class);
		actionNameMap.put("while", WhileDoAction.class);
		actionNameMap.put("break", BreakAction.class);
		actionNameMap.put("finish", FinishAction.class);
		actionNameMap.put("throw", ThrowAction.class);
		actionNameMap.put("try-catch", TryCatchAction.class);

		actionNameMap.put("log", LogAction.class);

		actionNameMap.put("kill-time", KillTimeAction.class);
		actionNameMap.put("fork", ForkAction.class);

		actionNameMap.put("transform", XslTransformAction.class);

		actionNameMap.put("scription", BeanShellAction.class);
		actionNameMap.put("sql", DBAction.class);
		actionNameMap.put("transaction", TXAction.class);

		actionNameMap.put("callout", CalloutAction.class);
	}

	public static Action getInstance(Element actionEl) {
		try {
			Action action = (Action) actionInstances.get(actionEl);
			if (action == null) {
				String actionName = actionEl.getName();
				Class clazz = (Class) actionNameMap.get(actionName);
				if (!(actionNameMap.containsKey(actionName))) {
					throw new ActionException(
							"Init action error, unrecognized '" + actionName
									+ "'");
				}
				AbstractAction absAction = null;
				try {
					absAction = (AbstractAction) clazz.newInstance();
				} catch (InstantiationException e) {
					logger.error("init action error...", e);
				} catch (IllegalAccessException e) {
					logger.error("init action error...", e);
				}
				absAction.init(actionEl);
				actionInstances.put(actionEl, absAction);
				return absAction;
			}

			return action;
		} catch (Exception e) {
			throw new ActionException(e, "");
		}
	}

	public static void registerAction(String name, Class<?> actionClass) {
		if ((actionNameMap.containsKey(name)) && (!(name.equals("callout")))) {
			throw new RuntimeException("Action named [" + name
					+ "] already registered");
		}
		actionNameMap.put(name, actionClass);
	}
}