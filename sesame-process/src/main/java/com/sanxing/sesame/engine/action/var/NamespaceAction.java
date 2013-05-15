package com.sanxing.sesame.engine.action.var;

import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class NamespaceAction
    extends AbstractAction
    implements Constant
{
    private String method;

    private Namespace namespace;

    private String varName;

    private String xpath;

    @Override
    public void doinit( Element config )
    {
        try
        {
            method = config.getAttributeValue( "method", "set" );

            String prefix = config.getAttributeValue( "prefix", "" );
            String uri = config.getAttributeValue( "uri", "" );
            namespace = Namespace.getNamespace( prefix, uri );

            varName = config.getAttributeValue( "var" );
            xpath = config.getChildTextTrim( "xpath", config.getNamespace() );
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
            Variable target = getVariable( ctx, varName, xpath );
            if ( method.equals( "set" ) )
            {
                setNamespace( target );
                return;
            }
            if ( method.equals( "add" ) )
            {
                addNamespace( target );
                return;
            }
            if ( method.equals( "remove" ) )
            {
                removeNamespace( target );
            }
        }
        catch ( ActionException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new ActionException( this, e );
        }
    }

    private void removeNamespace( Variable target )
    {
        if ( target.getVarType() == 0 )
        {
            Element el = (Element) target.get();
            if ( namespace.getURI().length() == 0 )
            {
                Namespace ns = el.getNamespace( namespace.getPrefix() );
                el.removeNamespaceDeclaration( ns );
            }
            else
            {
                el.removeNamespaceDeclaration( namespace );
            }
        }
        else if ( target.getVarType() == 5 )
        {
            List list = (List) target.get();
            for ( Iterator localIterator = list.iterator(); localIterator.hasNext(); )
            {
                Object obj = localIterator.next();
                if ( obj instanceof Element )
                {
                    Element el = (Element) obj;
                    el.removeNamespaceDeclaration( namespace );
                }
            }
        }
        else
        {
            throw new ActionException( this, "00004" );
        }
    }

    private void addNamespace( Variable target )
    {
        if ( target.getVarType() == 0 )
        {
            Element el = (Element) target.get();
            el.addNamespaceDeclaration( namespace );
        }
        else if ( target.getVarType() == 5 )
        {
            List list = (List) target.get();
            for ( Iterator localIterator = list.iterator(); localIterator.hasNext(); )
            {
                Object obj = localIterator.next();
                if ( obj instanceof Element )
                {
                    Element el = (Element) obj;
                    el.addNamespaceDeclaration( namespace );
                }
            }
        }
        else
        {
            throw new ActionException( this, "00005" );
        }
    }

    private void setNamespace( Variable target )
    {
        if ( target.getVarType() == 0 )
        {
            Element el = (Element) target.get();
            el.setNamespace( namespace );
        }
        else if ( target.getVarType() == 3 )
        {
            Attribute attribute = (Attribute) target.get();
            attribute.setNamespace( namespace );
        }
        else if ( target.getVarType() == 5 )
        {
            List list = (List) target.get();
            for ( Iterator localIterator = list.iterator(); localIterator.hasNext(); )
            {
                Object obj = localIterator.next();
                if ( obj instanceof Element )
                {
                    Element el = (Element) obj;
                    el.setNamespace( namespace );
                }
                else if ( obj instanceof Attribute )
                {
                    Attribute attribute = (Attribute) obj;
                    attribute.setNamespace( namespace );
                }
            }
        }
        else
        {
            throw new ActionException( this, "00006" );
        }
    }
}