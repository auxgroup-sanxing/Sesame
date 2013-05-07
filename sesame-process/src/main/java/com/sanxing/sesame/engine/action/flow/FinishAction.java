package com.sanxing.sesame.engine.action.flow;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;

public class FinishAction
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
    public void dowork( DataContext context )
    {
        try
        {
            context.getExecutionContext().closeDebugging();
            context.getExecutionContext().setCurrentAction( "exit" );
        }
        catch ( InterruptedException localInterruptedException )
        {
        }
        throw new AbortException();
    }
}