package com.sanxing.sesame.wssecurity.jaas;

import javax.security.auth.callback.Callback;

public class NonceCallback
    implements Callback
{
    private byte[] nonce = null;

    public byte[] getNonce()
    {
        return this.nonce;
    }

    public void setNonce( byte[] nonce )
    {
        this.nonce = nonce;
    }
}