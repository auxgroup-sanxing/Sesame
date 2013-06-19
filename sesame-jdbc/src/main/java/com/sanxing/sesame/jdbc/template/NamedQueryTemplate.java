package com.sanxing.sesame.jdbc.template;

import java.util.List;
import java.util.Map;

public interface NamedQueryTemplate
{
    public abstract Map<String, Object> queryRow( String sql, Map<String, Object> parameters, Map<String, Integer> paramTypes );

    public abstract List<Map<String, Object>> query( String sql, Map<String, Object> parameters, Map<String, Integer> paramTypes );

    public abstract List<Map<String, Object>> query( String sql, Map<String, Object> parameters,
                                                     Map<String, Integer> paramTypes, int pageNo, int pageSize );

    public abstract Map<String, Object> queryRow( String sql, Map<String, Object> parameters );

    public abstract List<Map<String, Object>> query( String sql, Map<String, Object> parameters );

    public abstract List<Map<String, Object>> query( String sql, Map<String, Object> parameters, int pageNo, int pageSize );

    public abstract <T> T queryRow( String sql, Object parameters, Class<T> rowClazz );

    public abstract <T> List<T> query( String sql, Object parameters, Class<T> rowClazz );

    public abstract <T> List<T> query( String sql, Object parameters, Class<T> rowClazz, int pageNo, int pageSize );
}