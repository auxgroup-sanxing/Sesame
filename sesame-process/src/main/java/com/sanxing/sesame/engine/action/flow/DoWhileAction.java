package com.sanxing.sesame.engine.action.flow;

import java.util.Iterator;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;

public class DoWhileAction
    extends AbstractAction
    implements Constant
{
    private static final Logger LOG = LoggerFactory.getLogger( DoWhileAction.class );

    private String xpath;

    private Element actionEl;

    private Element contextEl;

    @Override
    public void doinit( Element actionEl )
    {
        xpath = actionEl.getChildTextTrim( "xpath", actionEl.getNamespace() );
        this.actionEl = actionEl;
        contextEl = new Element( "context" );
    }

    @Override
    public void dowork( DataContext ctx )
    {
        ExecutionContext executionContext = ctx.getExecutionContext();
        do
        {
            try
            {
                Iterator iterator = actionEl.getChild( "do" ).getChildren().iterator();
                ActionUtil.bachInvoke( ctx, iterator );

                if ( executionContext.isDebugging() )
                {
                    Element xpathEl = actionEl.getChild( "xpath" );
                    synchronized ( executionContext )
                    {
                        executionContext.setCurrentAction( xpathEl.getAttributeValue( "id", "" ) );
                        LOG.debug( "[Action id=" + xpathEl.getAttributeValue( "id" ) + "] I am waiting..." + getName() );

                        executionContext.wait();
                    }
                }
            }
            catch ( BreakException e )
            {
            }
            catch ( InterruptedException localInterruptedException )
            {
            }
        }
        while ( getFlag( ctx ) );
    }

    private boolean getFlag( DataContext ctx )
    {
        Variable booleanVar = select( contextEl, xpath, ctx );
        boolean bool = ( (Boolean) booleanVar.get() ).booleanValue();
        return bool;
    }

    @Override
    public void doworkInDehydrateState( DataContext ctx )
    {
        Iterator iterator = actionEl.getChild( "do" ).getChildren().iterator();
        ActionUtil.bachInvoke( ctx, iterator );
    }
}