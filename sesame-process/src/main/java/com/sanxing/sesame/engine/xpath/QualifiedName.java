package com.sanxing.sesame.engine.xpath;

import java.io.Serializable;

public class QualifiedName
    implements Serializable
{
    private static final long serialVersionUID = 2734958615642751535L;

    private final String namespaceURI;

    private final String localName;

    public QualifiedName( String namespaceURI, String localName )
    {
        if ( namespaceURI == null )
        {
            namespaceURI = "";
        }
        this.namespaceURI = namespaceURI;
        this.localName = localName;
    }

    @Override
    public int hashCode()
    {
        return ( localName.hashCode() ^ namespaceURI.hashCode() );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null )
        {
            return false;
        }

        QualifiedName other = (QualifiedName) o;

        return ( ( namespaceURI.equals( other.namespaceURI ) ) && ( localName.equals( other.localName ) ) );
    }

    public String getClarkForm()
    {
        if ( "".equals( namespaceURI ) )
        {
            return localName;
        }
        return "{" + namespaceURI + "}" + ":" + localName;
    }
}