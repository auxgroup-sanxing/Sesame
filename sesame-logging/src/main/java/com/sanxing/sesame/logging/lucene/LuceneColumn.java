package com.sanxing.sesame.logging.lucene;

public class LuceneColumn {
	private String name;
	private String value;
	private boolean indexAnalyzed;

	public LuceneColumn(String name, String value, boolean analyzed) {
		this.name = name;
		this.value = value;
		this.indexAnalyzed = analyzed;
	}

	public LuceneColumn(LuceneColumn col) {
		this.name = col.name;
		this.value = col.value;
		this.indexAnalyzed = col.indexAnalyzed;
	}

	public boolean isIndexAnalyzed() {
		return this.indexAnalyzed;
	}

	public void setIndexAnalyzed(boolean indexAnalyzed) {
		this.indexAnalyzed = indexAnalyzed;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}