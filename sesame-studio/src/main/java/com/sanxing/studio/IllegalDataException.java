package com.sanxing.studio;

public class IllegalDataException
    extends IllegalArgumentException
{
    private static final long serialVersionUID = -7150118147493867168L;

    IllegalDataException( String data, String construct, String reason )
    {
        super( "The data \"" + data + "\" is not legal" + construct + ": " + reason + "." );
    }

    IllegalDataException( String data, String construct )
    {
        super( "The data \"" + data + "\" is not legal " + construct + "." );
    }

    public IllegalDataException( String reason )
    {
        super( reason );
    }
}