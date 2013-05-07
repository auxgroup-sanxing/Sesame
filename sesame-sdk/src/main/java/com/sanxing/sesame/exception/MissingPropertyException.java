package com.sanxing.sesame.exception;

import javax.jbi.JBIException;

public class MissingPropertyException
    extends JBIException
{
    private static final long serialVersionUID = -5836161326213956167L;

    private final String property;

    public MissingPropertyException( String property )
    {
        super( "Cannot use this component as the property '" + property + "' was not configured" );
        this.property = property;
    }

    public String getProperty()
    {
        return property;
    }
}