package com.sanxing.sesame.jdbc.template;

public interface IndexedUpdateTemplate
{
    public abstract int update( String sql, Object[] parameters, int[] paramTypes );

    public abstract int update( String sql, Object[] parameters );
}