package com.sanxing.sesame.jdbc.template;

import com.sanxing.sesame.jdbc.template.impl.TemplateHelper;
import com.sanxing.sesame.jdbc.template.tx.TXTemplate;

public class TemplateManager {
	public static TXTemplate getTXTemplate(int txType) {
		return TemplateHelper.getTXTemplate(txType);
	}

	public static DataAccessTemplate getDataAccessTemplate(String dsName) {
		return TemplateHelper.getDataAccessTemplate(dsName);
	}

	public static IndexedQueryTemplate getIndexedQueryTemplate(String dsName) {
		return TemplateHelper.getDataAccessTemplate(dsName);
	}

	public static IndexedUpdateTemplate getIndexedUpdateTemplate(String dsName) {
		return TemplateHelper.getDataAccessTemplate(dsName);
	}

	public static NamedQueryTemplate getNamedQueryTemplate(String dsName) {
		return TemplateHelper.getDataAccessTemplate(dsName);
	}

	public static NamedUpdateTemplate getNamedUpdateTemplate(String dsName) {
		return TemplateHelper.getDataAccessTemplate(dsName);
	}

	public static CustomizedQueryTemplate getCustomizedQueryTemplate(
			String dsName) {
		return TemplateHelper.getDataAccessTemplate(dsName);
	}

	public static CustomizedUpdateTemplate getCustomizedUpdateTemplate(
			String dsName) {
		return TemplateHelper.getDataAccessTemplate(dsName);
	}
}