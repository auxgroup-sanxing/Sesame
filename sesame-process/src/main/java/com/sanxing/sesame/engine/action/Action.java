package com.sanxing.sesame.engine.action;

import org.jdom.Element;

import com.sanxing.sesame.engine.context.DataContext;

public interface Action
{
    public abstract String getName();

    public abstract void init( Element config );

    public abstract void work( DataContext ctx );

    public abstract boolean isRollbackable();
}