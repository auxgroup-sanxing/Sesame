package com.sanxing.sesame.engine.action;

import com.sanxing.sesame.engine.context.DataContext;
import org.jdom2.Element;

public abstract interface Action {
	public abstract String getName();

	public abstract void init(Element paramElement);

	public abstract void work(DataContext paramDataContext);

	public abstract boolean isRollbackable();
}