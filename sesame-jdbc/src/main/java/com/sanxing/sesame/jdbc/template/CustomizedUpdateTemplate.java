package com.sanxing.sesame.jdbc.template;

public abstract interface CustomizedUpdateTemplate
{
    public abstract int update( String paramString, ParametersMapping paramParametersMapping );
}