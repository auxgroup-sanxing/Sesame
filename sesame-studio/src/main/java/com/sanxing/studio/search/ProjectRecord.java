package com.sanxing.studio.search;

public class ProjectRecord extends Record implements SearcherName {
	public Column getDescriptionColumn() {
		return getColumnByName("project description");
	}

	public Column getNameColumn() {
		return getColumnByName("project name");
	}

	public String getSearcherName() {
		return "project";
	}
}