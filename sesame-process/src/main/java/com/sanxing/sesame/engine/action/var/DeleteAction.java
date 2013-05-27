package com.sanxing.sesame.engine.action.var;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class DeleteAction
    extends AbstractAction
    implements Constant
{
    String varName;

    String xpath;

    @Override
    public void doinit( Element config )
    {
        varName = config.getAttributeValue( Constant.ATTR_VAR_NAME );
        xpath = config.getChildTextTrim( Constant.ELE_XPATH, config.getNamespace() );
    }

    @Override
    public void dowork( DataContext ctx )
    {
        Variable toBeDeleted = ctx.getVariable( varName );
        if ( xpath != null )
        {
            if ( xpath.equalsIgnoreCase( "xmlns" ) )
            {
                Element ele = (Element) toBeDeleted.get();
                removeNameSpace( ele );
            }
            else
            {
                toBeDeleted = select( (Element) toBeDeleted.get(), xpath, ctx );
                if ( toBeDeleted.getVarType() <= 2 )
                {
                    ( (Content) toBeDeleted.get() ).detach();
                }
                else if ( toBeDeleted.getVarType() == 3 )
                {
                    ( (Attribute) toBeDeleted.get() ).detach();
                }
                else if ( toBeDeleted.getVarType() == 5 )
                {
                    List list = (List) toBeDeleted.get();
                    for ( int i = 0; i < list.size(); ++i )
                    {
                        Element ele = (Element) list.get( i );
                        ele.detach();
                    }
                }
            }
        }
        else
        {
            ctx.delVariable( varName );
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