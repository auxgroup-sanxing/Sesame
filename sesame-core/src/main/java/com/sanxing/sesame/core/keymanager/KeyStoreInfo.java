package com.sanxing.sesame.core.keymanager;

public class KeyStoreInfo
{
    private String name;

    private String keystorePath;

    private String storePass;

    private String description;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getKeystorePath()
    {
        return keystorePath;
    }

    public void setKeystorePath( String keystorePath )
    {
        this.keystorePath = keystorePath;
    }

    public String getStorePass()
    {
        return storePass;
    }

    public void setStorePass( String storePass )
    {
        this.storePass = storePass;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }
}