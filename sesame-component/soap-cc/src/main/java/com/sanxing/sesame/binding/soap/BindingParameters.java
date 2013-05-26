package com.sanxing.sesame.binding.soap;

public class BindingParameters
{
    private String soapType = "SoapBody";

    private String xpathExpression;

    private String httpHeader;

    private String verifyKeyProvider;

    private String signingKeyProvider;

    private String encryptionKeyProvider;

    private Boolean signSwitch = Boolean.valueOf( false );

    private Boolean encryptionSwitch = Boolean.valueOf( false );

    private Boolean verifySwitch = Boolean.valueOf( false );

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

    public void setXpathExpression( String expression )
    {
        this.xpathExpression = expression;
    }

    public void setHttpPara( String attribute )
    {
        this.httpHeader = attribute;
    }

    public String getSoapType()
    {
        return this.soapType;
    }

    public String getXpathExpression()
    {
        return this.xpathExpression;
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