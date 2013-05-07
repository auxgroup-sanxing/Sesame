package com.sanxing.sesame.engine.context;

import java.io.Serializable;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Variable
    implements Cloneable, Serializable
{
    public static final int ELEMENT = 0;

    public static final int TEXT = 1;

    public static final int CDATA = 2;

    public static final int ATTRIBUTE = 3;

    public static final int NAMESPACE = 4;

    public static final int LIST = 5;

    public static final int BOOLEAN = 6;

    public static final int STRING = 7;

    public static final int NUMBER = 8;

    static XMLOutputter out = new XMLOutputter();

    private int varType = 0;

    private final Object innerVar;

    static
    {
        out.setFormat( Format.getPrettyFormat() );
    }

    public int getVarType()
    {
        return varType;
    }

    public Variable( Object obj, int type )
    {
        varType = type;
        innerVar = obj;
    }

    public Object get()
    {
        return innerVar;
    }

    public void append( Variable var )
    {
        if ( varType == 0 )
        {
            Element ele = (Element) innerVar;
            if ( var.getVarType() <= 2 )
            {
                ele.addContent( (Content) var.get() );
            }
            else if ( var.getVarType() == 3 )
            {
                ele.setAttribute( (Attribute) var.get() );
            }
            else
            {
                ele.setNamespace( (Namespace) var.get() );
            }
        }
        else
        {
            throw new RuntimeException( "target var must be element" );
        }
    }

    @Override
    public Object clone()
    {
        Object innerVar = null;
        if ( get() instanceof Content )
        {
            innerVar = ( (Content) get() ).clone();
        }
        else if ( get() instanceof Attribute )
        {
            innerVar = ( (Attribute) get() ).clone();
        }
        else
        {
            innerVar = get();
        }
        return new Variable( innerVar, varType );
    }

    @Override
    public String toString()
    {
        switch ( getVarType() )
        {
            case 0:
                return out.outputString( (Element) innerVar );
            case 1:
                return out.outputString( (Text) innerVar );
            case 2:
                return out.outputString( (Text) innerVar );
            case 3:
                Attribute attr = (Attribute) innerVar;
                return attr.getQualifiedName() + ":" + attr.getValue();
            case 4:
                Namespace ns = (Namespace) innerVar;
                return ns.getPrefix() + ":" + ns.getURI();
            case 5:
                List list = (List) innerVar;
                return "list size:[" + list.size() + "]";
        }

        return innerVar.toString();
    }
}