package com.sanxing.sesame.engine.context;

import bsh.EvalError;
import bsh.Interpreter;
import com.sanxing.sesame.engine.action.beanshell.BeanShellContext;
import com.sanxing.sesame.engine.action.callout.Reverter;
import com.sanxing.sesame.engine.action.jdbc.ConnectionUtil;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.transaction.TransactionManager;

public class ExecutionContext {
	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_ERRORHANDLING = 1;
	private int status;
	private List<TransactionManager> beginedTMS = new LinkedList();

	private Reverter reverter = new Reverter();
	private String uuid;
	private DataContext dataCtx;
	private boolean doCutpoint = false;

	private Map<String, Object> context = new HashMap();
	private TransactionManager currentTM;
	private AtomicBoolean debugging = new AtomicBoolean(false);

	private ArrayBlockingQueue<String> actQueue = new ArrayBlockingQueue(1);
	private Interpreter bsh;
	private boolean terminated = false;

	public String getCurrentAction() throws InterruptedException {
		return ((String) this.actQueue.take());
	}

	public void setCurrentAction(String currentAction)
			throws InterruptedException {
		this.actQueue.put(currentAction);
	}

	public ExecutionContext(String uuid) {
		this.uuid = uuid;
		this.dataCtx = DataContext.getInstance(uuid);
		this.dataCtx.setExecutionContext(this);
		this.bsh = new Interpreter();
	}

	public TransactionManager getCurrentTM() {
		return this.currentTM;
	}

	public void setCurrentTM(TransactionManager currentTM) {
		this.beginedTMS.add(currentTM);
		this.currentTM = currentTM;
	}

	public Interpreter getBshInterpreter() {
		return this.bsh;
	}

	public void close() {
		this.dataCtx.close();
		ConnectionUtil.clean(this.uuid);
		this.actQueue.clear();
		BeanShellContext bsc = (BeanShellContext) get("beanshell.context");
		if (bsc == null)
			return;
		try {
			bsc.close();
		} catch (EvalError e) {
			e.printStackTrace();
		}
	}

	public Reverter getReverter() {
		return this.reverter;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void put(String key, Object value) {
		this.context.put(key, value);
	}

	public Object get(String key) {
		return this.context.get(key);
	}

	public boolean isDoCutpoint() {
		return this.doCutpoint;
	}

	public void setDoCutpoint(boolean doCutpoint) {
		this.doCutpoint = doCutpoint;
	}

	public DataContext getDataContext() {
		return this.dataCtx;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isDebugging() {
		return this.debugging.get();
	}

	public void openDebugging() {
		this.debugging.set(true);
	}

	public void closeDebugging() {
		this.debugging.set(false);
	}

	public boolean isDehydrated() {
		return DehydrateManager.isDehydrated(getUuid());
	}

	public void terminate() {
		this.terminated = true;
	}

	public boolean isTerminated() {
		return this.terminated;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 1000; ++i)
			for (int j = 0; j < 10; ++j)
				new Interpreter();
	}
}