package com.sanxing.sesame.wtc;

import com.union.HsmAPI.gdHsmAPI;
import java.io.File;

public class Mac
{
    private String keyName;

    private String cfgName;

    public Mac()
    {
        this.keyName = "SE.00009230.zak";
        this.cfgName = "HsmSvr.CFG";
    }

    public byte[] mac( byte[] bytes )
    {
        try
        {
            String gdHsmPath = this.cfgName;
            String path = getClass().getClassLoader().getResource( gdHsmPath ).getPath();
            long start = System.currentTimeMillis();
            gdHsmAPI tt = new gdHsmAPI( new File( path ).getParent() + File.separator );
            tt.Init();
            long end = System.currentTimeMillis();
            System.out.println( "init elapse: " + ( end - start ) );

            start = System.currentTimeMillis();
            String mac = tt.UnionGenMac( this.keyName, bytes.length, bytes );
            end = System.currentTimeMillis();
            System.out.println( "generate elapse: " + ( end - start ) );

            return mac.getBytes();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verify( byte[] content, byte[] mac )
    {
        try
        {
            String gdHsmPath = this.cfgName;
            String path = getClass().getClassLoader().getResource( gdHsmPath ).getPath();

            long start = System.currentTimeMillis();
            gdHsmAPI tt = new gdHsmAPI( new File( path ).getParent() + File.separator );
            tt.Init();
            long end = System.currentTimeMillis();
            System.out.println( "init elapse: " + ( end - start ) );

            start = System.currentTimeMillis();
            int rtn = tt.UnionVerifyMac( this.keyName, content.length, content, new String( mac ) );
            end = System.currentTimeMillis();
            System.out.println( "verify elapse:" + ( end - start ) );

            if ( rtn >= 0 )
            {
                return true;
            }
            return false;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean store( String id, String keyvalue, String keychk )
    {
        try
        {
            String gdHsmPath = this.cfgName;
            String path = getClass().getClassLoader().getResource( gdHsmPath ).getPath();

            long start = System.currentTimeMillis();
            gdHsmAPI tt = new gdHsmAPI( new File( path ).getParent() + File.separator );
            tt.Init();
            long end = System.currentTimeMillis();
            System.out.println( "init elapse: " + ( end - start ) );

            start = System.currentTimeMillis();
            int rtn = tt.UnionStoreKey( id, keyvalue, keychk );
            end = System.currentTimeMillis();
            System.out.println( "verify elapse:" + ( end - start ) );
            if ( rtn >= 0 )
            {
                return true;
            }
            return false;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return false;
    }

    public String getKeyName()
    {
        return this.keyName;
    }

    public void setKeyName( String keyName )
    {
        this.keyName = keyName;
    }
}
