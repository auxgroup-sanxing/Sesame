package com.sanxing.sesame.jdbc.template;

public interface CustomizedUpdateTemplate
{
    public abstract int update( String sql, ParametersMapping paramMapping );
}