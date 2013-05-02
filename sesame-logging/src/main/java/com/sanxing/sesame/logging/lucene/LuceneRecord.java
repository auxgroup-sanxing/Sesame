package com.sanxing.sesame.logging.lucene;

import java.util.ArrayList;
import java.util.List;

public abstract class LuceneRecord implements Comparable<LuceneRecord> {
	public List<LuceneColumn> columns = new ArrayList();
	private String searcherName;

	public void addField(LuceneColumn column) {
		this.columns.add(column);
	}

	public List<LuceneColumn> getColumns() {
		return this.columns;
	}

	public LuceneColumn getColumnByName(String name) {
		for (LuceneColumn e : getColumns()) {
			if (e.getName().equalsIgnoreCase(name)) {
				return e;
			}
		}
		return null;
	}

	public void setIndexAnalyzed(boolean indexAnalyzed) {
		for (LuceneColumn column : this.columns)
			column.setIndexAnalyzed(indexAnalyzed);
	}

	public void join(LuceneRecord base, boolean indexAnalyzed) {
		List<LuceneColumn> baseColumns = base.getColumns();
		for (LuceneColumn e : baseColumns) {
			LuceneColumn ne = new LuceneColumn(e);
			ne.setIndexAnalyzed(indexAnalyzed);
			this.columns.add(ne);
		}
	}

	public void setSearcherName(String searcherName) {
		this.searcherName = searcherName;
	}

	public int compareTo(LuceneRecord o) {
		return -1;
	}

	public String toString() {
		String name = "{";
		for (LuceneColumn column : this.columns) {
			name = name + column.getName() + "=" + column.getValue()
					+ " isIndexAnalyzed=" + column.isIndexAnalyzed();
			name = name + "\t";
		}
		name = name + "}";
		return name;
	}
}