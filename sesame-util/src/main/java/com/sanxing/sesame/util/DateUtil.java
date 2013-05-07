package com.sanxing.sesame.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateUtil
{
    private static SimpleDateFormat fullFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

    private static SimpleDateFormat dateForamt1 = new SimpleDateFormat( "yyyy-MM-dd" );

    private static SimpleDateFormat dateForamt2 = new SimpleDateFormat( "yyyy/MM/dd" );

    private static SimpleDateFormat dateForamt3 = new SimpleDateFormat( "yyyyMMdd" );

    public static String getFullText( java.util.Date date )
    {
        return fullFormat.format( date );
    }

    public static String getDateStrWithDASH( java.util.Date date )
    {
        return dateForamt1.format( date );
    }

    public static String getDateStrWithSPLASH( java.util.Date date )
    {
        return dateForamt2.format( date );
    }

    public static String getDateStri( java.util.Date date )
    {
        return dateForamt3.format( date );
    }

    public static java.util.Date getDateFromShortStr( String date )
    {
        try
        {
            return dateForamt3.parse( date );
        }
        catch ( ParseException e )
        {
            throw new RuntimeException( "parse date str err" );
        }
    }

    public static String formatDate( String pattern, java.util.Date date )
    {
        SimpleDateFormat f = new SimpleDateFormat( pattern );
        return f.format( date );
    }

    public static int getdaysbetween( String date1, String date2 )
    {
        int iYear = Integer.parseInt( date1.substring( 0, 4 ) );
        int iMonth = Integer.parseInt( date1.substring( 4, 6 ) ) - 1;
        int iDay = Integer.parseInt( date1.substring( 6, 8 ) );
        GregorianCalendar ca1 = new GregorianCalendar( iYear, iMonth, iDay );

        iYear = Integer.parseInt( date2.substring( 0, 4 ) );
        iMonth = Integer.parseInt( date2.substring( 4, 6 ) ) - 1;
        iDay = Integer.parseInt( date2.substring( 6, 8 ) );
        GregorianCalendar ca2 = new GregorianCalendar( iYear, iMonth, iDay );

        int year1 = ca1.get( 1 );
        int year2 = ca2.get( 1 );

        int dayofYear1 = ca1.get( 6 );
        int dayofYear2 = ca2.get( 6 );

        int days = 0;
        int ip = 0;
        for ( int i = year1; i < year2; ++i )
        {
            if ( isLeapyear( i ) )
            {
                ip += 366;
            }
            else
            {
                ip += 365;
            }
        }

        int temp = ip + dayofYear2 - dayofYear1 + 1;
        return temp;
    }

    public static boolean isLeapyear( int year )
    {
        boolean isproyear = false;
        if ( ( ( ( year % 400 == 0 ) ? 1 : 0 ) | ( ( ( year % 100 != 0 ) && ( year % 4 == 0 ) ) ? 1 : 0 ) ) != 0 )
        {
            isproyear = true;
        }
        else
        {
            isproyear = false;
        }
        return isproyear;
    }

    public static java.sql.Date getAfterNDay( java.util.Date date, int n )
        throws Exception
    {
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        long nDay = date.getTime() + n * 86400000L;
        String nDayString = new SimpleDateFormat( "yyyy/MM/dd" ).format( new java.util.Date( nDay ) );
        return GetSqlDateFromStr( nDayString );
    }

    public static java.sql.Date GetSqlDateFromStr( String strDt )
    {
        strDt = strDt.replace( ' ', '-' );
        strDt = strDt.replace( '.', '-' );
        strDt = strDt.replace( '/', '-' );
        return java.sql.Date.valueOf( strDt );
    }

    public static String getDateString( String date )
    {
        return date.substring( 0, 4 ) + "-" + date.substring( 4, 6 ) + "-" + date.substring( 6, 8 );
    }
}