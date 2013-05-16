package com.sanxing.sesame.engine.action.flow;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class DecisionAction
    extends AbstractAction
{
    private Element actionEl;

    private Element contextEl;

    @Override
    public void doinit( Element actionEl )
    {
        this.actionEl = actionEl;
        contextEl = new Element( "context" );
    }

    @Override
    public void dowork( DataContext ctx )
    {
        boolean exit = false;

        List list = actionEl.getChildren( "if" );
        for ( Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Element ifEl = (Element) iter.next();

            String xpath = ifEl.getChildTextTrim( "xpath", ifEl.getNamespace() );
            Variable booleanVar = select( contextEl, xpath, ctx );
            Boolean bool = (Boolean) booleanVar.get();
            Element thenEl = ifEl.getChild( "then", ifEl.getNamespace() );
            if ( bool.booleanValue() )
            {
                Iterator actions = thenEl.getChildren().iterator();
                ActionUtil.bachInvoke( ctx, actions );
                exit = true;
                break;
            }
        }

        if ( ( !( exit ) ) && ( actionEl.getChild( "default", actionEl.getNamespace() ) != null ) )
        {
            Iterator actions = actionEl.getChild( "default", actionEl.getNamespace() ).getChildren().iterator();
            ActionUtil.bachInvoke( ctx, actions );
        }
    }

    @Override
    public void doworkInDehydrateState( DataContext context )
    {
        boolean exit = false;

        List list = actionEl.getChildren( "if" );

        for ( int i = 0; i < list.size(); ++i )
        {
            Element ifEl = (Element) list.get( i );
            Element thenEl = ifEl.getChild( "then", ifEl.getNamespace() );
            Iterator actions = thenEl.getChildren().iterator();
            ActionUtil.bachInvoke( context, actions );
        }

        if ( actionEl.getChild( "default", actionEl.getNamespace() ) != null )
        {
            Iterator actions = actionEl.getChild( "default", actionEl.getNamespace() ).getChildren().iterator();
            ActionUtil.bachInvoke( context, actions );
        }
    }
}