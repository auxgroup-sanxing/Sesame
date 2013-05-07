package com.sanxing.sesame.engine.exceptions;

import com.sanxing.sesame.exceptions.AppException;

public class MockException
    extends AppException
{
    private static final long serialVersionUID = 1815014407945369978L;

    public MockException( String key, String errorCode )
    {
        super( key );
        setErrorCode( errorCode );
    }
}