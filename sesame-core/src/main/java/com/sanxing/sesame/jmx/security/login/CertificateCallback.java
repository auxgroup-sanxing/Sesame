package com.sanxing.sesame.jmx.security.login;

import java.security.cert.X509Certificate;

import javax.security.auth.callback.Callback;

public class CertificateCallback
    implements Callback
{
    private X509Certificate certificate;

    public X509Certificate getCertificate()
    {
        return certificate;
    }

    public void setCertificate( X509Certificate certificate )
    {
        this.certificate = certificate;
    }
}