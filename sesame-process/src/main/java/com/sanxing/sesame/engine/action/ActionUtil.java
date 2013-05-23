package com.sanxing.sesame.engine.action;

import java.util.Iterator;

import org.jaxen.NamespaceContext;
import org.jdom.Element;

import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;

import static com.sanxing.sesame.engine.ExecutionEnv.*;

public class ActionUtil
{
    public static void bachInvoke( DataContext ctx, Iterator<?> actions )
    {
        ExecutionContext executionCtx = ctx.getExecutionContext();
        NamespaceContext namespaceCtx = (NamespaceContext) executionCtx.get( NAMESPACE_CONTEXT );

        while ( actions.hasNext() )
        {
            Element actionEl = (Element) actions.next();

            Action action = ActionFactory.getInstance( actionEl );

            if ( action instanceof AbstractAction )
            {
                AbstractAction abstractAction = (AbstractAction) action;

                abstractAction.setNamespaceContext( namespaceCtx );
            }
            action.work( ctx );
        }
    }
}