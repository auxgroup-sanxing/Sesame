package com.sanxing.sesame.engine.action.flow;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;

public class BreakAction
    extends AbstractAction
{
    @Override
    public boolean isRollbackable()
    {
        return false;
    }

    @Override
    public void doinit( Element config )
    {
    }

    @Override
    public void dowork( DataContext config )
    {
        throw new BreakException();
    }
}