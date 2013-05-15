package com.sanxing.sesame.engine.action.var;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class CloneAction
    extends AbstractAction
    implements Constant
{
    String targetVarName;

    String xPath;

    String sourceVarName;

    @Override
    public void doinit( Element config )
    {
        sourceVarName = config.getAttributeValue( "var" );
        targetVarName = config.getAttributeValue( "to-var" );
        xPath = config.getChildTextTrim( "xpath", config.getNamespace() );
    }

    @Override
    public void dowork( DataContext ctx )
    {
        Variable var = getVariable( ctx, sourceVarName, xPath );
        Variable target = (Variable) var.clone();
        ctx.addVariable( targetVarName, target );
    }
}