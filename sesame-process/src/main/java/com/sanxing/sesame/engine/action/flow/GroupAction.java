package com.sanxing.sesame.engine.action.flow;

import java.util.Iterator;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.context.DataContext;

public class GroupAction
    extends AbstractAction
{
    Element config;

    @Override
    public void doinit( Element config )
    {
        this.config = config;
    }

    @Override
    public void dowork( DataContext ctx )
    {
        Iterator actions = config.getChildren().iterator();
        ActionUtil.bachInvoke( ctx, actions );
    }
}