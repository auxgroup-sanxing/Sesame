package com.sanxing.sesame.jaxp;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public interface ExtendedNamespaceContext
    extends NamespaceContext
{
    public abstract Iterator getPrefixes();
}