package com.sanxing.sesame.engine.action.var;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.context.VariableFactory;

public class AssignAction
    extends AbstractAction
    implements Constant
{
    String targetVarName;

    String xPath;

    String sourceVarName;

    String rawText;

    String targetVarType;

    String rawValue;

    private boolean cloneObject;

    @Override
    public void doinit( Element config )
    {
        targetVarName = config.getAttributeValue( Constant.ATTR_TO_VAR_NAME );

        sourceVarName = config.getAttributeValue( Constant.ATTR_VAR_NAME );

        cloneObject = config.getAttributeValue( "clone", "false" ).equals( "true" );

        targetVarType = config.getAttributeValue( Constant.ATTR_TYPE );

        xPath = config.getChildTextTrim( Constant.ELE_XPATH, config.getNamespace() );

        rawValue = config.getChildTextTrim( Constant.ELE_RAW_VALUE, config.getNamespace() );
    }

    @Override
    public void dowork( DataContext ctx )
    {
        Variable targetVar;
        if ( sourceVarName == null )
        {
            targetVar = VariableFactory.getIntance( rawValue, targetVarType );
        }
        else
        {
            Variable srcVar = ctx.getVariable( sourceVarName );
            if ( ( srcVar.getVarType() != 0 ) && ( xPath != null ) && ( xPath.length() > 0 ) )
            {
                ActionException ae = new ActionException( this, "00003" );
                ae.setErrMsgArgs( new String[] { sourceVarName } );
            }
            targetVar = getVariable( ctx, sourceVarName, xPath );
        }
        if ( cloneObject )
        {
            targetVar = (Variable) targetVar.clone();
        }

        ctx.addVariable( targetVarName, targetVar );
    }
}