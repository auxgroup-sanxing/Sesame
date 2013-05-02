package com.sanxing.sesame.jdbc.tools;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD })
public @interface Column {
	public abstract String column_name();

	public abstract COLUMN_TYPE type();

	public abstract String len();

	public abstract boolean notNull();

	public abstract boolean pk();

	public static enum COLUMN_TYPE {
		VARCHAR, INTEGER, DECIMAL;
	}
}