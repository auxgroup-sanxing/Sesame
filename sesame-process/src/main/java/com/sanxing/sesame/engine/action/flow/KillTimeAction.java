package com.sanxing.sesame.engine.action.flow;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;

public class KillTimeAction
    extends AbstractAction
{
    long waitTime = 0L;

    @Override
    public void doinit( Element config )
    {
        waitTime = Integer.parseInt( config.getAttributeValue( "wait" ) );
    }

    @Override
    public void dowork( DataContext config )
    {
        try
        {
            Thread.sleep( waitTime );
        }
        catch ( InterruptedException localInterruptedException )
        {
        }
    }
}