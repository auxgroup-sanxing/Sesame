package com.sanxing.sesame.jmx.security.auth;

import java.security.GeneralSecurityException;

import javax.security.auth.Subject;

public interface AuthenticationService
{
    public abstract void authenticate( Subject subject, String domain, final String user, final Object credentials )
        throws GeneralSecurityException;
}