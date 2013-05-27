package com.sanxing.sesame.engine.action.var;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class RenameAction
    extends AbstractAction
    implements Constant
{
    String varName;

    String xpath;

    String newName;

    @Override
    public void doinit( Element config )
    {
        varName = config.getAttributeValue( Constant.ATTR_VAR_NAME );
        xpath = config.getAttributeValue( Constant.ELE_XPATH );
        newName = config.getAttributeValue( Constant.ATTR_NEW_NAME );
    }

    @Override
    public void dowork( DataContext ctx )
    {
        Variable toBeRenamed = getVariable( ctx, varName, xpath );
        if ( toBeRenamed.getVarType() == 0 )
        {
            ( (Element) toBeRenamed.get() ).setName( newName );
        }
        else if ( toBeRenamed.getVarType() == 3 )
        {
            ( (Attribute) toBeRenamed.get() ).setName( newName );
        }
        else if ( toBeRenamed.getVarType() == 5 )
        {
            List list = (List) toBeRenamed.get();
            for ( int i = 0; i < list.size(); ++i )
            {
                Element ele = (Element) list.get( i );
                ele.setName( newName );
            }
        }
    }
}