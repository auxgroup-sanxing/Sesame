package com.sanxing.sesame.jdbc.template;

import java.util.List;
import java.util.Map;

public abstract interface NamedQueryTemplate
{
    public abstract Map<String, Object> queryRow( String paramString, Map<String, Object> paramMap,
                                                  Map<String, Integer> paramMap1 );

    public abstract List<Map<String, Object>> query( String paramString, Map<String, Object> paramMap,
                                                     Map<String, Integer> paramMap1 );

    public abstract List<Map<String, Object>> query( String paramString, Map<String, Object> paramMap,
                                                     Map<String, Integer> paramMap1, int paramInt1, int paramInt2 );

    public abstract Map<String, Object> queryRow( String paramString, Map<String, Object> paramMap );

    public abstract List<Map<String, Object>> query( String paramString, Map<String, Object> paramMap );

    public abstract List<Map<String, Object>> query( String paramString, Map<String, Object> paramMap, int paramInt1,
                                                     int paramInt2 );

    public abstract <T> T queryRow( String paramString, Object paramObject, Class<T> paramClass );

    public abstract <T> List<T> query( String paramString, Object paramObject, Class<T> paramClass );

    public abstract <T> List<T> query( String paramString, Object paramObject, Class<T> paramClass, int paramInt1,
                                       int paramInt2 );
}