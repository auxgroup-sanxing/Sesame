package com.sanxing.studio.team.member;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.sanxing.sesame.util.Base64;
import com.sanxing.studio.Application;

public class Member
{
    public static String getPublicKey()
    {
        KeyPair keyPair = Application.getKeyPair();
        byte[] bytes = keyPair.getPublic().getEncoded();

        return new String( Base64.encode( bytes ) );
    }

    public static void main( String[] args )
    {
        try
        {
            KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA" );
            generator.initialize( 512 );
            KeyPair keyPair = generator.generateKeyPair();
            keyPair.getPublic();

            Cipher cipher = Cipher.getInstance( "RSA" );
            cipher.init( 1, keyPair.getPublic() );
            byte[] bytes = cipher.doFinal( "12345678abcxzkljoqiueqoewiurqopieruqpoweruqo".getBytes() );

            System.out.println( keyPair.getPublic().getEncoded().length );

            cipher.init( 2, keyPair.getPrivate() );

            bytes = cipher.doFinal( bytes );

            System.out.println( new String( bytes ) );
        }
        catch ( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }
        catch ( NoSuchPaddingException e )
        {
            e.printStackTrace();
        }
        catch ( InvalidKeyException e )
        {
            e.printStackTrace();
        }
        catch ( IllegalBlockSizeException e )
        {
            e.printStackTrace();
        }
        catch ( BadPaddingException e )
        {
            e.printStackTrace();
        }
    }
}