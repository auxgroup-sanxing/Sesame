package com.sanxing.sesame.util;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;

public abstract class LazyDOMSource
    extends DOMSource
{
    private boolean initialized;

    @Override
    public Node getNode()
    {
        if ( !( initialized ) )
        {
            setNode( loadNode() );
            initialized = true;
        }
        return super.getNode();
    }

    protected abstract Node loadNode();
}