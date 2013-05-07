package com.sanxing.adp.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;

public class WSDLUtil
{
    public static List<ExtensibilityElement> findExtensibilityElement( List extensibilityElements, String elementType )
    {
        List elements = new ArrayList();
        if ( extensibilityElements != null )
        {
            Iterator iter = extensibilityElements.iterator();
            while ( iter.hasNext() )
            {
                ExtensibilityElement elment = (ExtensibilityElement) iter.next();
                if ( !( elment.getElementType().getLocalPart().equalsIgnoreCase( elementType ) ) )
                {
                    continue;
                }
                elements.add( elment );
            }
        }

        return elements;
    }
}