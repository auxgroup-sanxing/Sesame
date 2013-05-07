package com.sanxing.sesame.pwd;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordCipher
{
    private static final Logger LOG = LoggerFactory.getLogger( PasswordCipher.class );

    byte[] encryptKey;

    DESedeKeySpec spec;

    SecretKeyFactory keyFactory;

    SecretKey theKey;

    Cipher cipher;

    IvParameterSpec IvParameters;

    private static PasswordCipher instance;

    public PasswordCipher()
    {
        this( "Sesame is an ESB made in China." );
    }

    public PasswordCipher( String strEncryptKey )
    {
        try
        {
            cipher = Cipher.getInstance( "DESede/CBC/PKCS5Padding" );

            encryptKey = strEncryptKey.getBytes();

            spec = new DESedeKeySpec( encryptKey );

            keyFactory = SecretKeyFactory.getInstance( "DESede" );

            theKey = keyFactory.generateSecret( spec );

            IvParameters = new IvParameterSpec( new byte[] { 26, 84, 55, 8, 19, 102, 66, 73 } );
        }
        catch ( Exception ex )
        {
            LOG.debug( ex.getMessage(), ex );
        }
    }

    public byte[] encrypt( String password )
    {
        byte[] encrypted_pwd = null;
        try
        {
            cipher.init( Cipher.ENCRYPT_MODE, theKey, IvParameters );

            encrypted_pwd = password.getBytes();

            encrypted_pwd = cipher.doFinal( encrypted_pwd );
        }
        catch ( Exception ex )
        {
            LOG.debug( ex.getMessage(), ex );
        }

        return encrypted_pwd;
    }

    public String decrypt( byte[] password )
    {
        String decrypted_password = null;
        byte[] decryptedPassword = password;
        try
        {
            cipher.init( Cipher.DECRYPT_MODE, theKey, IvParameters );
            decryptedPassword = cipher.doFinal( decryptedPassword );

        }
        catch ( Exception ex )
        {
            LOG.debug( ex.getMessage(), ex );
        }
        decrypted_password = new String( decryptedPassword );
        return decrypted_password;
    }

    public static PasswordCipher getInstance()
    {
        if ( instance == null )
        {
            instance = new PasswordCipher();
        }
        return instance;
    }
}