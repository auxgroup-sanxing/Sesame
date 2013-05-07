package com.sanxing.sesame.jmx.security.auth;

import java.security.GeneralSecurityException;

import javax.security.auth.Subject;

public abstract interface AuthenticationService
{
    public abstract void authenticate( Subject paramSubject, String paramString1, String paramString2,
                                       Object paramObject )
        throws GeneralSecurityException;
}