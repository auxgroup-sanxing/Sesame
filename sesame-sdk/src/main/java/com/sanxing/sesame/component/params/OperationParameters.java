package com.sanxing.sesame.component.params;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class OperationParameters
    extends HashMap<String, Parameter>
{
    private final String operationName;

    private static final long serialVersionUID = 6681588165577452559L;

    public OperationParameters( String operationName )
    {
        this.operationName = operationName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public Element toElement()
    {
        Element root = new Element( "operation-params" );
        root.setAttribute( "name", operationName );
        Iterator keys = keySet().iterator();
        while ( keys.hasNext() )
        {
            String key = (String) keys.next();
            Parameter param = get( key );
            root.addContent( param.toElement() );
        }
        return root;
    }

    public static OperationParameters fromElement( Element element )
    {
        String operationName = element.getAttributeValue( "name" );
        OperationParameters op = new OperationParameters( operationName );
        List<Element> params = element.getChildren( "param" );
        for ( Element paramEle : params )
        {
            Parameter param = Parameter.fromElement( paramEle );
            op.put( param.getName(), param );
        }

        return op;
    }
}