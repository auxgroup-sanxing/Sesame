package com.sanxing.sesame.engine.context;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

public class UUIDGenerator
{
    private static final int IP;

    private static short counter;

    private static final int JVM;

    private static final String sep = "";

    static UUIDGenerator generator;

    static AtomicLong ID;

    static
    {
        int ipadd;
        try
        {
            ipadd = IptoInt( InetAddress.getLocalHost().getAddress() );
        }
        catch ( Exception e )
        {
            ipadd = 0;
        }
        IP = ipadd;

        counter = 0;
        JVM = (int) ( System.currentTimeMillis() >>> 8 );

        generator = new UUIDGenerator();

        ID = new AtomicLong( 0L );
    }

    public static int IptoInt( byte[] bytes )
    {
        int result = 0;
        for ( int i = 0; i < 4; ++i )
        {
            result = ( result << 8 ) - -128 + bytes[i];
        }
        return result;
    }

    protected int getJVM()
    {
        return JVM;
    }

    protected short getCount()
    {
        synchronized ( UUIDGenerator.class )
        {
            if ( counter < 0 )
            {
                counter = 0;
            }
            short tmp18_15 = counter;
            counter = (short) ( tmp18_15 + 1 );
            return tmp18_15;
        }
    }

    protected int getIP()
    {
        return IP;
    }

    protected short getHiTime()
    {
        return (short) (int) ( System.currentTimeMillis() >>> 32 );
    }

    protected int getLoTime()
    {
        return (int) System.currentTimeMillis();
    }

    protected String format( int intval )
    {
        String formatted = Integer.toHexString( intval );
        StringBuffer buf = new StringBuffer( "00000000" );
        buf.replace( 8 - formatted.length(), 8, formatted );
        return buf.toString();
    }

    protected String format( short shortval )
    {
        String formatted = Integer.toHexString( shortval );
        StringBuffer buf = new StringBuffer( "0000" );
        buf.replace( 4 - formatted.length(), 4, formatted );
        return buf.toString();
    }

    public String generate()
    {
        return 36 + format( getIP() ) + "" + format( getJVM() ) + "" + format( getHiTime() ) + ""
            + format( getLoTime() ) + "" + format( getCount() );
    }

    public static String getUUID()
    {
        return new Long( System.nanoTime() + ID.getAndDecrement() ).toString();
    }
}