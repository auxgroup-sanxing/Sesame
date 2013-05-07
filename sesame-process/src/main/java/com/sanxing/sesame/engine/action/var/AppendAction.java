package com.sanxing.sesame.engine.action.var;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.context.VariableFactory;

public class AppendAction
    extends AbstractAction
    implements Constant
{
    String targetVarName;

    String xPath;

    String sourceVarName;

    String rawText;

    String targetVarType;

    String rawValue;

    int index = -1;

    @Override
    public void doinit( Element config )
    {
        try
        {
            targetVarName = config.getAttributeValue( "to-var" );

            sourceVarName = config.getAttributeValue( "var" );

            targetVarType = config.getAttributeValue( "type", "" );

            xPath = config.getChildTextTrim( "xpath" );

            rawValue = config.getChildTextTrim( "raw-value" );
            if ( ( config.getAttributeValue( "index" ) == null ) || ( config.getAttributeValue( "index" ).equals( "" ) ) )
            {
                index = -1;
                return;
            }
            index = Integer.parseInt( config.getAttributeValue( "index", "-1" ) );
        }
        catch ( Exception e )
        {
            throw new ActionException( this, "00001" );
        }
    }

    @Override
    public void dowork( DataContext ctx )
    {
        try
        {
            try
            {
                Variable toBeAppended;
                try
                {
                    if ( sourceVarName == null )
                    {
                        toBeAppended = VariableFactory.getIntance( rawValue, targetVarType );
                    }
                    else
                    {
                        toBeAppended = getVariable( ctx, sourceVarName, xPath );
                    }
                }
                catch ( Exception e )
                {
                    throw new ActionException( this, e );
                }
                if ( ctx.getVariable( targetVarName ) == null )
                {
                    throw new ActionException( this, "00002" );
                }

                Element target = (Element) ctx.getVariable( targetVarName ).get();

                if ( toBeAppended.getVarType() == 0 )
                {
                    if ( index != -1 )
                    {
                        target.addContent( index, (Content) toBeAppended.get() );
                        return;
                    }
                    target.addContent( (Content) toBeAppended.get() );
                    return;
                }
                if ( toBeAppended.getVarType() == 4 )
                {
                    target.setNamespace( (Namespace) toBeAppended.get() );
                    return;
                }
                if ( toBeAppended.getVarType() == 5 )
                {
                    List<Element> oldcon = (List) toBeAppended.get();
                    List newcon = new ArrayList();
                    for ( Element el : oldcon )
                    {
                        Element newele = (Element) el.clone();
                        newcon.add( newele );
                    }
                    target.addContent( newcon );
                    return;
                }
                if ( toBeAppended.getVarType() == 3 )
                {
                    target.setAttribute( (Attribute) toBeAppended.get() );
                    return;
                }
                if ( toBeAppended.getVarType() == 1 )
                {
                    target.addContent( (Text) toBeAppended.get() );
                    return;
                }
                target.addContent( toBeAppended.get().toString() );
            }
            catch ( ActionException e )
            {
                throw e;
            }
        }
        catch ( Exception e )
        {
            String desc = "$" + targetVarName + " << " + ( ( sourceVarName != null ) ? "$" + sourceVarName : rawValue );
            throw new ActionException( this, e );
        }
    }
}