package com.sanxing.sesame.core.keymanager;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

public class ServiceKeyProvider
{
    private String name;

    private String alias;

    private String keystoreName;

    private boolean paired;

    private String keyPass;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias( String alias )
    {
        this.alias = alias;
    }

    public String getKeystoreName()
    {
        return keystoreName;
    }

    public void setKeystoreName( String keystoreName )
    {
        this.keystoreName = keystoreName;
    }

    public boolean isPri()
    {
        return paired;
    }

    public void setPri( boolean pri )
    {
        paired = pri;
    }

    public String getKeyPass()
    {
        return keyPass;
    }

    public void setKeyPass( String keyPass )
    {
        this.keyPass = keyPass;
    }

    public Key getKey()
    {
        try
        {
            KeyStoreInfo storeInfo = KeyStoreManager.getInstance().getKeyStore( keystoreName );
            String storePass = storeInfo.getStorePass();
            FileInputStream fis1 = new FileInputStream( storeInfo.getKeystorePath() );
            KeyStore ks1 = KeyStore.getInstance( "jks" );
            ks1.load( fis1, storePass.toCharArray() );
            Key privateKey = ks1.getKey( alias, keyPass.toCharArray() );
            return privateKey;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "no such key [" + alias + "] or key password is wrong" );
        }
    }

    public Certificate getCert()
    {
        try
        {
            KeyStoreInfo storeInfo = KeyStoreManager.getInstance().getKeyStore( keystoreName );
            String storePass = storeInfo.getStorePass();
            FileInputStream fis1 = new FileInputStream( storeInfo.getKeystorePath() );
            KeyStore ks1 = KeyStore.getInstance( "jks" );
            ks1.load( fis1, storePass.toCharArray() );
            Certificate cert = ks1.getCertificate( alias );
            return cert;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "no such key [" + alias + "] or store password is wrong" );
        }
    }
}