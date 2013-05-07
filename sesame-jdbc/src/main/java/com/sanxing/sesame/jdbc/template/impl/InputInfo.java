package com.sanxing.sesame.jdbc.template.impl;

public class InputInfo
{
    private String sql = null;

    private String[] paramNames = null;

    public InputInfo( String sql, String[] paramNames )
    {
        this.sql = sql;
        this.paramNames = paramNames;
    }

    public String getSql()
    {
        return sql;
    }

    public String[] getParamNames()
    {
        return paramNames;
    }
}