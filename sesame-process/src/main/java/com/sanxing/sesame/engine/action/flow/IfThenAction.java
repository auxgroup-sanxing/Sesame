package com.sanxing.sesame.engine.action.flow;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class IfThenAction
    extends AbstractAction
    implements Constant
{
    String varName;

    String xpath;

    Element config;

    @Override
    public void doinit( Element config )
    {
        varName = config.getAttributeValue( Constant.ATTR_VAR_NAME );

        xpath = config.getChildTextTrim( Constant.ELE_XPATH, config.getNamespace() );

        this.config = config;
    }

    @Override
    public void dowork( DataContext ctx )
    {
        Variable var = ctx.getVariable( varName );
        Element ele = (Element) var.get();

        Variable booleanVar = select( ele, xpath, ctx );
        Boolean bool = (Boolean) booleanVar.get();
        if ( bool.booleanValue() )
        {
            Iterator thenActions = config.getChild( Constant.ELE_THEN, config.getNamespace() ).getChildren().iterator();
            ActionUtil.bachInvoke( ctx, thenActions );
        }
        else
        {
            boolean elseif = false;

            List list = config.getChildren( Constant.ELE_ELSE_IF );

            for ( int i = 0; i < list.size(); ++i )
            {
                Element elseifthen = (Element) list.get( i );

                varName = elseifthen.getAttributeValue( Constant.ATTR_VAR_NAME );
                String xpath = elseifthen.getChildTextTrim( Constant.ELE_XPATH, elseifthen.getNamespace() );

                var = ctx.getVariable( varName );
                ele = (Element) var.get();

                booleanVar = select( ele, xpath, ctx );
                bool = (Boolean) booleanVar.get();
                Iterator thenActions = elseifthen.getChild( Constant.ELE_THEN, config.getNamespace() ).getChildren().iterator();
                if ( bool.booleanValue() )
                {
                    elseif = true;
                    ActionUtil.bachInvoke( ctx, thenActions );
                    break;
                }
            }

            if ( ( !( elseif ) ) && ( config.getChild( Constant.ELE_ELSE, config.getNamespace() ) != null ) )
            {
                Iterator elseActions = config.getChild( Constant.ELE_ELSE, config.getNamespace() ).getChildren().iterator();
                ActionUtil.bachInvoke( ctx, elseActions );
            }
        }
    }
}