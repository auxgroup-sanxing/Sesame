package com.sanxing.sesame.engine.action.callout;

import com.sanxing.sesame.engine.action.flow.exceptions.Catcher;
import com.sanxing.sesame.engine.context.DataContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Reverse implements Comparable<Reverse> {
	private String group;
	private int index;
	private List<CatchClause> clauses = new ArrayList();
	private DataContext snapshot;

	public int compareTo(Reverse o) {
		if (this.index == o.index)
			return 0;
		if (this.index < o.index) {
			return -1;
		}
		return 1;
	}

	public CatchClause getClause(String exceptionKey) {
		for (Iterator iter = this.clauses.iterator(); iter.hasNext();) {
			CatchClause clause = (CatchClause) iter.next();
			if (Catcher.isCatchable(exceptionKey, clause.getExceptionKeys())) {
				return clause;
			}
		}
		return null;
	}

	public void put(String[] catches, List<?> actions, boolean instantly) {
		CatchClause clause = new CatchClause();
		clause.catches = catches;
		clause.actions = actions;
		clause.instantly = instantly;
		this.clauses.add(clause);
	}

	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public DataContext getSnapshot() {
		return this.snapshot;
	}

	public void setSnapshot(DataContext snapshot) {
		this.snapshot = snapshot;
	}

	public String toString() {
		return "Reverse [index=" + this.index + ", group=" + this.group
				+ ", catches=" + this.clauses + "]";
	}

	public static class CatchClause {
		private String[] catches;
		private List<?> actions;
		private boolean instantly;

		public String[] getExceptionKeys() {
			return this.catches;
		}

		public List<?> getActions() {
			return this.actions;
		}

		public boolean isInstantly() {
			return this.instantly;
		}
	}
}