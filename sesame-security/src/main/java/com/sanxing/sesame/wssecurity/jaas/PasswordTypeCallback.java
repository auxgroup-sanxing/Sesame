package com.sanxing.sesame.wssecurity.jaas;

import javax.security.auth.callback.Callback;

public class PasswordTypeCallback
    implements Callback
{
    private String passwordType;

    public String getPasswordType()
    {
        return this.passwordType;
    }

    public void setPasswordType( String passwordType )
    {
        this.passwordType = passwordType;
    }
}