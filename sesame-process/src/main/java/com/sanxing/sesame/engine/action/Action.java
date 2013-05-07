package com.sanxing.sesame.engine.action;

import org.jdom.Element;

import com.sanxing.sesame.engine.context.DataContext;

public abstract interface Action
{
    public abstract String getName();

    public abstract void init( Element paramElement );

    public abstract void work( DataContext paramDataContext );

    public abstract boolean isRollbackable();
}