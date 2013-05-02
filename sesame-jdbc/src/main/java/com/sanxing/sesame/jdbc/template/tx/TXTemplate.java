package com.sanxing.sesame.jdbc.template.tx;

public abstract interface TXTemplate {
	public abstract Object handle(DataAccessProcessor paramDataAccessProcessor);
}