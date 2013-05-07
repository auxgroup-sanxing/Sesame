package com.sanxing.sesame.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtil
{
    static Map map = new HashMap();

    public static Method getMethodByName( String name, Class clazz )
    {
        if ( !( map.containsKey( clazz ) ) )
        {
            Method[] methods = clazz.getMethods();
            map.put( clazz, methods );
        }
        Method[] methods = (Method[]) map.get( clazz );
        for ( int i = 0; i < methods.length; ++i )
        {
            if ( name.equals( methods[i].getName() ) )
            {
                return methods[i];
            }
        }
        return null;
    }
}