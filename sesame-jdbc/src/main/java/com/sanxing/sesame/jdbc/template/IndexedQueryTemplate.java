package com.sanxing.sesame.jdbc.template;

import java.util.List;
import java.util.Map;

public interface IndexedQueryTemplate
{
    public abstract Map<String, Object> queryRow( String sql, Object[] parameters, int[] paramTypes );

    public abstract List<Map<String, Object>> query( String sql, Object[] parameters, int[] paramTypes );

    public abstract List<Map<String, Object>> query( String sql, Object[] parameters, int[] paramTypes, int pageNo, int pageSize );

    public abstract Map<String, Object> queryRow( String sql, Object[] parameters );

    public abstract List<Map<String, Object>> query( String sql, Object[] parameters );

    public abstract List<Map<String, Object>> query( String sql, Object[] parameters, int pageNo, int pageSize );
}