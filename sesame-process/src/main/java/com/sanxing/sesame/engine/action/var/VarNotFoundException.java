package com.sanxing.sesame.engine.action.var;

public class VarNotFoundException
    extends RuntimeException
{
    private static final long serialVersionUID = -5194523828814305773L;

    public VarNotFoundException( String message )
    {
        super( "404|" + message );
    }
}