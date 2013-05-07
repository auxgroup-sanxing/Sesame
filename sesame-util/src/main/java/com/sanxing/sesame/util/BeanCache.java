package com.sanxing.sesame.util;

import java.util.HashMap;
import java.util.Map;

public class BeanCache
{
    private final Map<Class, BeanMetaData> beanMetaDataMap = new HashMap();

    public synchronized void putBeanMetaData( Class clazz, BeanMetaData beanMetaData )
    {
        beanMetaDataMap.put( clazz, beanMetaData );
    }

    public synchronized BeanMetaData getBeanMetaData( Class clazz )
    {
        BeanMetaData bMetaData = beanMetaDataMap.get( clazz );
        return bMetaData;
    }
}