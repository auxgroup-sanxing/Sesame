package com.sanxing.sesame.engine.action.flow.exceptions;

public class Catcher
{
    public static boolean isCatchable( String exceptionKey, String[] catches )
    {
        if ( catches.length == 0 )
        {
            return true;
        }

        String key = exceptionKey;
        boolean matched = match( key, catches );
        while ( ( !( matched ) ) && ( key.indexOf( 46 ) > 0 ) )
        {
            key = key.substring( 0, key.lastIndexOf( 46 ) );
            matched = match( key, catches );
        }
        return matched;
    }

    private static boolean match( String key, String[] catches )
    {
        for ( String item : catches )
        {
            if ( key.equalsIgnoreCase( item ) )
            {
                return true;
            }
        }
        return false;
    }

    public static void main( String[] args )
    {
        String[] catches = { "callout", "abc", "hah", "general" };
        long timeMillis = System.nanoTime();
        boolean catched = isCatchable( "callout", catches );

        System.out.println( "Elapsed: " + ( System.nanoTime() - timeMillis ) + "ns" );
        System.out.println( "Catched: " + catched );
    }
}