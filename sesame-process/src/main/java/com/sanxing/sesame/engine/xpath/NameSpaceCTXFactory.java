package com.sanxing.sesame.engine.xpath;

import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;

public class NameSpaceCTXFactory
{
    private static NamespaceContext instance;

    public static NamespaceContext getInstance()
    {
        if ( instance == null )
        {
            instance = new SimpleNamespaceContext();
        }
        return instance;
    }
}