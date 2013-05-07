package com.sanxing.sesame.engine;

import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;
import org.jdom.Element;

public class FlowInfo
{
    private final SimpleNamespaceContext namespaceCtx = new SimpleNamespaceContext();

    private String name;

    private String description;

    private String author;

    private Element flowDefination;

    public void addNSMapping( String URI, String prefix )
    {
        namespaceCtx.addNamespace( prefix, URI );
    }

    public NamespaceContext getNamespaceContext()
    {
        return namespaceCtx;
    }

    public Element getFlowDefination()
    {
        return flowDefination;
    }

    public void setFlowDefination( Element flowDefination )
    {
        this.flowDefination = flowDefination;
        if ( !( validate() ) )
        {
            throw new RuntimeException( "invalid flow defination" );
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor( String author )
    {
        this.author = author;
    }

    private boolean validate()
    {
        return true;
    }
}