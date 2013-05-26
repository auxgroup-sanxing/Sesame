package com.sanxing.sesame.binding.soap;

import org.jdom.xpath.XPath;

public class BindingParameters
{
    private String soapType = "SoapBody";

    private XPath operationXpath;

    private String httpHeader;

    private String verifyKeyProvider;

    private String signingKeyProvider;

    private String encryptionKeyProvider;

    private Boolean signSwitch;

    private Boolean encryptionSwitch;

    private Boolean verifySwitch;

    public void setVerifyKeyProvider( String keyProvider )
    {
        this.verifyKeyProvider = keyProvider;
    }

    public void setSigningKeyProvider( String KeyProvider )
    {
        this.signingKeyProvider = KeyProvider;
    }

    public void setEncryptionKeyProvider( String KeyProvider )
    {
        this.encryptionKeyProvider = KeyProvider;
    }

    public String getVerifyKeyProvider()
    {
        return this.verifyKeyProvider;
    }

    public String getSigningKeyProvider()
    {
        return this.signingKeyProvider;
    }

    public String getEncryptionKeyProvider()
    {
        return this.encryptionKeyProvider;
    }

    public void setSoapType( String attribute )
    {
        this.soapType = attribute;
    }

    public void setXpath( XPath newInstance )
    {
        this.operationXpath = newInstance;
    }

    public void setHttpPara( String attribute )
    {
        this.httpHeader = attribute;
    }

    public String getSoapType()
    {
        return this.soapType;
    }

    public XPath getXpath()
    {
        return this.operationXpath;
    }

    public String getHttpPara()
    {
        return this.httpHeader;
    }

    public Boolean getSignSwitch()
    {
        return this.signSwitch;
    }

    public void setSignSwitch( String para )
    {
        this.signSwitch = Boolean.valueOf( para.equals( "on" ) );
    }

    public Boolean getencryptionSwitch()
    {
        return this.encryptionSwitch;
    }

    public void setencryptionSwitch( String para )
    {
        this.encryptionSwitch = Boolean.valueOf( para.equals( "on" ) );
    }

    public Boolean getverifySwitch()
    {
        return this.verifySwitch;
    }

    public void setverifySwitch( String para )
    {
        this.verifySwitch = Boolean.valueOf( para.equals( "on" ) );
    }
}