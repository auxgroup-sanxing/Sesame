package com.sanxing.sesame.engine.context;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

public class TryCatchContext implements Comparable<TryCatchContext> {
	private int levelIndex = 1;
	public static final Logger logger = LoggerFactory.getLogger(TryCatchContext.class);

	private boolean tring = true;
	private String[] catchableExceptions;
	private List<Element> ExceptionHandleFlow;
	private int index;
	private List<TryCatchContext> childrenTCCs = new LinkedList();
	private TryCatchContext parentTCC;
	private DataContext messageContext;

	public int getLevelIndex() {
		return this.levelIndex;
	}

	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	public int compareTo(TryCatchContext o) {
		return (this.index - o.index);
	}

	public void close() {
		Iterator iter = this.childrenTCCs.iterator();
		if (this.messageContext != null) {
			this.messageContext.close();
		}
		while (iter.hasNext()) {
			TryCatchContext tcc = (TryCatchContext) iter.next();
			tcc.close();
			iter.remove();
		}
	}

	public List<Element> getExceptionHandleFlow() {
		return this.ExceptionHandleFlow;
	}

	public void setExceptionHandleFlow(List<Element> exceptionHandleFlow) {
		this.ExceptionHandleFlow = exceptionHandleFlow;
	}

	public String[] getCatchableExceptions() {
		return this.catchableExceptions;
	}

	public void setCatchableExceptions(String[] catchableExceptions) {
		this.catchableExceptions = catchableExceptions;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public TryCatchContext getParentTCC() {
		return this.parentTCC;
	}

	public void setParentTCC(TryCatchContext parentTCC) {
		this.parentTCC = parentTCC;
		this.levelIndex = (parentTCC.getLevelIndex() * 10 + parentTCC
				.getChildren().size());
	}

	public DataContext getMessageContext() {
		return this.messageContext;
	}

	public void setMessageContext(DataContext messageContext) {
		this.messageContext = messageContext;
	}

	public void addChild(TryCatchContext context) {
		this.childrenTCCs.add(context);
		context.setParentTCC(this);
	}

	public List<TryCatchContext> getChildren() {
		return this.childrenTCCs;
	}

	public void endTrying() {
		this.tring = false;
	}

	public boolean isTring() {
		return this.tring;
	}

	public String toString() {
		return "TryCatchContext{index=" + this.index + ", levelIndex="
				+ this.levelIndex + '}';
	}
}