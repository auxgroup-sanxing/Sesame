package com.sanxing.sesame.logging.monitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLCondition
{
    public static final String LIKE = "like";

    public static final String ParamType_Number = "NUM";

    public static final String ParamType_String = "STR";

    public static final String ParamType_Datetime = "DATE";

    private String condition = "";

    public static void main( String[] args )
    {
        SQLCondition cond = new SQLCondition();
        cond.add( "serviceName", "like", "value" );
        cond.add( "channel", "=", "value2" );
        cond.addNum( "serialNumber", ">", "1" );
        cond.addNum( "serialNumber", "<", "100" );
        cond.addTimeStamp( "startTime", ">", "2010-11-05", "08", "10", "00" );
        cond.addDate( "startTime", "<=", "2010", "11", "06" );
        System.out.println( cond.getCondition() );
        cond.clear();
    }

    public void add( String column, String operator, Object value, String type, boolean allowEmpty )
    {
        if ( ( !( allowEmpty ) ) && ( ( ( value == null ) || ( "".equals( value ) ) ) ) )
        {
            return;
        }
        if ( type.equals( "STR" ) )
        {
            if ( "like".equalsIgnoreCase( operator ) )
            {
                if ( type.equals( "DATE" ) )
                {
                    SQLCondition tmp49_48 = this;
                    tmp49_48.condition = tmp49_48.condition + column + " LIKE '" + value + "%' AND ";
                }
                else
                {
                    SQLCondition tmp92_91 = this;
                    tmp92_91.condition = tmp92_91.condition + column + " LIKE '%" + value + "%' AND ";
                }
            }
            else
            {
                SQLCondition tmp135_134 = this;
                tmp135_134.condition = tmp135_134.condition + column + " " + operator + " '" + value + "' AND ";
            }

        }
        else if ( type.equals( "NUM" ) )
        {
            if ( "like".equalsIgnoreCase( operator ) )
            {
                SQLCondition tmp206_205 = this;
                tmp206_205.condition = tmp206_205.condition + column + " = " + " " + value + " AND ";
            }
            else
            {
                SQLCondition tmp254_253 = this;
                tmp254_253.condition = tmp254_253.condition + column + " " + operator + " " + value + " AND ";
            }

        }
        else if ( type.equals( "DATE" ) )
        {
            SQLCondition tmp316_315 = this;
            tmp316_315.condition =
                tmp316_315.condition + column + " " + operator + "to_timestamp(" + "'" + value + "'"
                    + ",'yyyy-mm-dd hh24:mi:ss') " + " AND ";
        }
    }

    public boolean isNumeric( String str )
    {
        Pattern pattern = Pattern.compile( "[0-9]*" );
        Matcher isNum = pattern.matcher( str );

        return ( isNum.matches() );
    }

    public void add( String column, String operator, String value )
    {
        add( column, operator, value, "STR", false );
    }

    public void addNum( String column, String operator, String value )
    {
        if ( !( isNumeric( value ) ) )
        {
            return;
        }
        add( column, operator, value, "NUM", false );
    }

    public void addDate( String column, String operator, String year, String month, String day )
    {
        if ( ( isEmpty( year ) ) || ( isEmpty( month ) ) || ( isEmpty( day ) ) )
        {
            return;
        }

        if ( month.length() == 1 )
        {
            month = "0" + month;
        }

        if ( day.length() == 1 )
        {
            day = "0" + day;
        }
        String value = year + "-" + month + "-" + day;
        addTimeStamp( column, operator, value, "00", "00", "00" );
    }

    public void addTimeStamp( String column, String operator, String day, String hour, String minute, String second )
    {
        if ( ( isEmpty( day ) ) || ( isEmpty( hour ) ) || ( isEmpty( minute ) ) || ( isEmpty( second ) ) )
        {
            return;
        }

        if ( hour.length() == 1 )
        {
            hour = "0" + hour;
        }

        if ( minute.length() == 1 )
        {
            minute = "0" + minute;
        }

        if ( second.length() == 1 )
        {
            second = "0" + second;
        }
        String value = day + " " + hour + ":" + minute + ":" + second;
        add( column, operator, value, "DATE", false );
    }

    public void addTimeStamp( String column, String operator, String timestamp )
    {
        if ( isEmpty( timestamp ) )
        {
            return;
        }
        add( column, operator, timestamp, "DATE", false );
    }

    private boolean isEmpty( String buffer )
    {
        return ( ( buffer == null ) || ( "".equals( buffer ) ) );
    }

    public String getCondition()
    {
        if ( condition.indexOf( "WHERE" ) != -1 )
        {
            return condition;
        }

        if ( !( isEmpty( condition ) ) )
        {
            if ( condition.substring( condition.length() - 4, condition.length() ).equals( "AND " ) )
            {
                condition = condition.substring( 0, condition.length() - 4 );
            }
            condition = " WHERE " + condition;
        }

        return condition;
    }

    public void clear()
    {
        condition = "";
    }

    @Override
    public String toString()
    {
        return getCondition();
    }
}