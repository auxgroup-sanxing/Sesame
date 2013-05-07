package com.sanxing.sesame.engine.action.flow;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class ForEachAction
    extends AbstractAction
    implements Constant
{
    String varName;

    String xpath;

    String childVarName;

    Element config;

    @Override
    public void doinit( Element config )
    {
        varName = config.getAttributeValue( "var" );

        xpath = config.getChildTextTrim( "xpath" );

        childVarName = config.getAttributeValue( "as" );

        this.config = config;
    }

    @Override
    public String toString()
    {
        return "ForEachAction{varName='" + varName + '\'' + ", xpath='" + xpath + '\'' + ", childVarName='"
            + childVarName + '\'' + '}';
    }

    @Override
    public void dowork( DataContext ctx )
    {
        Variable var = ctx.getVariable( varName );
        Element ele = (Element) var.get();
        Variable listVar = null;
        try
        {
            listVar = select( ele, xpath, ctx );
        }
        catch ( Exception e )
        {
            ActionException ae = new ActionException( this, e );
            ae.setErrorCode( "00011" );
            throw ae;
        }
        if ( listVar.get() instanceof Element )
        {
            Element child = (Element) listVar.get();
            Variable childVar = new Variable( child, 0 );
            ctx.addVariable( childVarName, childVar );
            Iterator iterActions = config.getChild( "actions" ).getChildren().iterator();
            try
            {
                ActionUtil.bachInvoke( ctx, iterActions );
            }
            catch ( BreakException localBreakException1 )
            {
            }
        }
        else
        {
            List elements = (List) listVar.get();

            Iterator eleIter = elements.iterator();
            while ( eleIter.hasNext() )
            {
                Element child = (Element) eleIter.next();
                Variable childVar = new Variable( child, 0 );
                ctx.addVariable( childVarName, childVar );
                Iterator iterActions = config.getChild( "actions" ).getChildren().iterator();
                try
                {
                    ActionUtil.bachInvoke( ctx, iterActions );
                }
                catch ( BreakException e )
                {
                    return;
                }
            }
        }
    }

    @Override
    public void doworkInDehydrateState( DataContext ctx )
    {
        Iterator iterActions = config.getChild( "actions" ).getChildren().iterator();
        try
        {
            ActionUtil.bachInvoke( ctx, iterActions );
        }
        catch ( BreakException localBreakException )
        {
        }
    }
}