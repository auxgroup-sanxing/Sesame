package com.sanxing.sesame.engine.action.cutpoint;

import com.sanxing.sesame.engine.action.Action;
import com.sanxing.sesame.engine.context.DataContext;
import org.jdom2.Element;

public abstract interface ActionCutpoint {
	public abstract void beforeInit(Element paramElement);

	public abstract void afterInit(Element paramElement);

	public abstract void beforWork(DataContext paramDataContext,
			Action paramAction);

	public abstract void afterWork(DataContext paramDataContext,
			Action paramAction);
}