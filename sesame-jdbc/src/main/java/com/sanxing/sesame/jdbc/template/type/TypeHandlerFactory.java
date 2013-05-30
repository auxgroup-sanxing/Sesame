package com.sanxing.sesame.jdbc.template.type;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sanxing.sesame.jdbc.DataAccessException;

public class TypeHandlerFactory
{
    private static Map<Class, TypeHandler> typeHandlerMap = new HashMap();

    static
    {
        typeHandlerMap.put( String.class, new StringTypeHandler() );
        typeHandlerMap.put( boolean.class, new BooleanTypeHandler() );
        typeHandlerMap.put( Boolean.class, new BooleanTypeHandler() );
        typeHandlerMap.put( short.class, new ShortTypeHandler() );
        typeHandlerMap.put( Short.class, new ShortTypeHandler() );
        typeHandlerMap.put( int.class, new IntegerTypeHandler() );
        typeHandlerMap.put( Integer.class, new IntegerTypeHandler() );
        typeHandlerMap.put( long.class, new LongTypeHandler() );
        typeHandlerMap.put( Long.class, new LongTypeHandler() );
        typeHandlerMap.put( BigDecimal.class, new BigDecimalTypeHandler() );
        typeHandlerMap.put( byte[].class, new BytesTypeHandler() );
        typeHandlerMap.put( Byte[].class, new BytesTypeHandler() );
        typeHandlerMap.put( Date.class, new DateTypeHandler() );
        typeHandlerMap.put( java.util.Date.class, new DateTypeHandler() );
        typeHandlerMap.put( XMLGregorianCalendar.class, new XMLGregorianCalendarTypeHandler() );
        typeHandlerMap.put( Time.class, new TimeTypeHandler() );
        typeHandlerMap.put( Timestamp.class, new TimestampTypeHandler() );
    }

    public static TypeHandler getTypeHandler( Class clazz )
    {
        TypeHandler typeHandler = typeHandlerMap.get( clazz );
        if ( typeHandler == null )
        {
            if ( Timestamp.class.isAssignableFrom( clazz ) )
            {
                typeHandler = typeHandlerMap.get( Timestamp.class );
            }
            else if ( Date.class.isAssignableFrom( clazz ) )
            {
                typeHandler = typeHandlerMap.get( Date.class );
            }
            else if ( java.util.Date.class.isAssignableFrom( clazz ) )
            {
                typeHandler = typeHandlerMap.get( java.util.Date.class );
            }
            else if ( Time.class.isAssignableFrom( clazz ) )
            {
                typeHandler = typeHandlerMap.get( Time.class );
            }
            else if ( BigDecimal.class.isAssignableFrom( clazz ) )
            {
                typeHandler = typeHandlerMap.get( BigDecimal.class );
            }
        }
        if ( typeHandler == null )
        {
            throw new DataAccessException( "type handler is null" );
        }
        return typeHandler;
    }
}