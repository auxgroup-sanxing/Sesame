package com.sanxing.sesame.engine.action.flow.exceptions;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.exceptions.AppException;

public class ThrowAction
    extends AbstractAction
{
    private String exceptionKey;

    private String exceptionMsg;

    @Override
    public void doinit( Element actionEl )
    {
        exceptionKey = actionEl.getAttributeValue( "exception-key" );
        exceptionMsg = actionEl.getAttributeValue( "message" );
    }

    @Override
    public void dowork( DataContext context )
        throws AppException
    {
        throw new RuntimeException( exceptionKey + "|" + exceptionMsg );
    }
}