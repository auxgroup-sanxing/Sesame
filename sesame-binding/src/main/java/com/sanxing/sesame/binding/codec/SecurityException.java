package com.sanxing.sesame.binding.codec;

import com.sanxing.sesame.binding.BindingException;

public class SecurityException
    extends BindingException
{
    private static final long serialVersionUID = 8755294324454694865L;

    public SecurityException( String message )
    {
        super( message );
    }

    public SecurityException( Throwable cause )
    {
        super( cause );
    }

    public SecurityException( String message, Throwable cause )
    {
        super( message, cause );
    }
}