package com.sanxing.sesame.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sanxing.sesame.collection.IgnoreCaseMap;

public class BeanMetaData
{
    private final Map<String, Method> readMethodMap = new IgnoreCaseMap();

    private final Map<String, Method> writeMethodMap = new IgnoreCaseMap();

    private final List<String> propNames = new ArrayList();

    public void initMetaData( Class clazz )
    {
        Method[] methods = clazz.getMethods();
        for ( int i = 0; i < methods.length; ++i )
        {
            Method method = methods[i];
            String methodName = method.getName();
            String propName = getPropNameByReadMethod( methodName, method );
            if ( propName != null )
            {
                readMethodMap.put( propName, method );
                propNames.add( propName );
            }
            else
            {
                propName = getPropNameByWriteMethod( methodName, method );
                if ( propName != null )
                {
                    writeMethodMap.put( propName, method );
                }
            }
        }
    }

    public Iterator<String> getPropNameIterator()
    {
        return propNames.iterator();
    }

    private String getPropNameByReadMethod( String methodName, Method method )
    {
        String propName = null;
        if ( ( methodName.startsWith( "get" ) ) && ( !( methodName.equals( "getClass" ) ) )
            && ( methodName.length() > 3 ) && ( method.getParameterTypes().length == 0 ) )
        {
            propName = methodName.substring( 3 );
            return propName;
        }

        if ( ( methodName.startsWith( "is" ) ) && ( methodName.length() > 2 )
            && ( method.getParameterTypes().length == 0 ) )
        {
            propName = methodName.substring( 2 );
            return propName;
        }

        return propName;
    }

    private String getPropNameByWriteMethod( String methodName, Method method )
    {
        String propName = null;
        if ( ( methodName.startsWith( "set" ) ) && ( methodName.length() > 3 )
            && ( method.getParameterTypes().length == 1 ) )
        {
            propName = methodName.substring( 3 );
        }

        return propName;
    }

    public Method getReadMethod( String propName )
    {
        Method method = readMethodMap.get( propName );
        if ( method == null )
        {
            method = readMethodMap.get( propName.replaceAll( "_", "" ) );
        }
        return method;
    }

    public Method getWriteMethod( String propName )
    {
        Method method = writeMethodMap.get( propName );
        if ( method == null )
        {
            method = writeMethodMap.get( propName.replaceAll( "_", "" ) );
        }
        return method;
    }
}