package com.sanxing.sesame.engine.action.cutpoint;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.Action;
import com.sanxing.sesame.engine.context.DataContext;

public interface ActionCutpoint
{
    public abstract void beforeInit( Element config );

    public abstract void afterInit( Element config );

    public abstract void beforWork( DataContext ctx, Action action );

    public abstract void afterWork( DataContext ctx, Action action );
}