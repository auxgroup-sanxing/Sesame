package com.sanxing.sesame.engine.action.var;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class RemoveNSAction
    extends AbstractAction
    implements Constant
{
    String targetVarName;

    String sourceVarName;

    @Override
    public void doinit( Element config )
    {
        targetVarName = config.getAttributeValue( Constant.ATTR_TO_VAR_NAME );
        sourceVarName = config.getAttributeValue( Constant.ATTR_VAR_NAME );
        if ( targetVarName == null )
        {
            targetVarName = sourceVarName;
        }
    }

    @Override
    public void dowork( DataContext ctx )
    {
        try
        {
            Variable toBeRemoved = getVariable( ctx, sourceVarName, null );
            if ( toBeRemoved.getVarType() == 0 )
            {
                Element ele = (Element) toBeRemoved.get();
                removeNameSpace( ele );
            }
        }
        catch ( Exception localException )
        {
        }
    }

    private void removeNameSpace( Element ele )
    {
        Namespace ns = ele.getNamespace();

        if ( ns != null )
        {
            ele.setNamespace( Namespace.NO_NAMESPACE );

            for ( int i = 0; i < ele.getChildren().size(); ++i )
            {
                removeNameSpace( (Element) ele.getChildren().get( i ) );
            }
        }
    }
}