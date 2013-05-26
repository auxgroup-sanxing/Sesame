package com.sanxing.sesame.wssecurity.jaas;

import javax.security.auth.callback.Callback;

public class GSSTokenCallback
    implements Callback
{
    private byte[] token = null;

    public byte[] getToken()
    {
        return this.token;
    }

    public void setToken( byte[] token )
    {
        this.token = token;
    }
}