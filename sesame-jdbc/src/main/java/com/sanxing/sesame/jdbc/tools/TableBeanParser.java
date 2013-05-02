package com.sanxing.sesame.jdbc.tools;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableBeanParser {
	public TableBeanMETA parse(Class tableBeanClazz) {
		TableBeanMETA meta = new TableBeanMETA();
		Class clazz = tableBeanClazz;
		meta.setClazzName(clazz.getSimpleName());
		meta.setTableName(unCamelize(meta.getClazzName()));
		if (clazz.isAnnotationPresent(Table.class)) {
			Table t = (Table) clazz.getAnnotation(Table.class);
			if ((t.tableName() != null) && (!(t.tableName().equals("")))) {
				meta.setTableName(t.tableName());
			}
		}

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			ColumnFieldMETA fm;
			if (field.isAnnotationPresent(Column.class))
				fm = parseFieldByAnnotation(meta, field);
			else {
				fm = parseByReflection(field);
			}
			meta.addField(fm);
		}
		return meta;
	}

	private ColumnFieldMETA parseByReflection(Field field) {
		ColumnFieldMETA fm = new ColumnFieldMETA();
		fm.setColumnName(unCamelize(field.getName()));

		if (field.getType().equals(String.class)) {
			fm.setColumnType("VARCHAR");
			fm.setLength(20);
		} else if ((field.getType().equals(Integer.class))
				|| (field.getType() == Integer.TYPE)) {
			fm.setColumnType("INTEGER");
		} else if (field.getType().equals(BigDecimal.class)) {
			fm.setColumnType("DECIMAL");
		}
		return fm;
	}

	private ColumnFieldMETA parseFieldByAnnotation(TableBeanMETA meta,
			Field field) {
		ColumnFieldMETA fm = new ColumnFieldMETA();

		Column column = (Column) field.getAnnotation(Column.class);
		if ((column.column_name() == null) || (column.column_name().equals("")))
			fm.setColumnName(unCamelize(field.getName()));
		else {
			fm.setColumnName(column.column_name());
		}

		if (column.pk()) {
			fm.setPk(true);
		}

		if (column.type().equals(Column.COLUMN_TYPE.VARCHAR)) {
			fm.setColumnType("VARCHAR");
			fm.setLength(Integer.parseInt(column.len()));
		} else if (column.type().equals(Column.COLUMN_TYPE.INTEGER)) {
			fm.setColumnType("INTEGER");
		} else if (column.type().equals(Column.COLUMN_TYPE.DECIMAL)) {
			fm.setColumnType("DECIMAL");
		} else {
			fm.setColumnType("VARCHAR");
			fm.setLength(Integer.parseInt(column.len()));
		}
		return fm;
	}

	private static String unCamelize(String inputString) {
		Pattern p = Pattern.compile("\\p{Lu}");
		Matcher m = p.matcher(inputString);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "_" + m.group().toUpperCase());
		}
		m.appendTail(sb);
		return sb.toString().trim().toUpperCase();
	}
}