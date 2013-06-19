package com.sanxing.sesame.jdbc.template;

import java.util.Map;

public interface NamedUpdateTemplate
{
    public abstract int update( String sql, Map<String, Object> parameters, Map<String, Integer> paramTypes );

    public abstract int update( String sql, Map<String, Object> parameters );

    public abstract int update( String sql, Object parameters );
}