package com.sanxing.sesame.jdbc.template.dialect;

public interface DataAccessDialect
{
    public abstract String getPagedSql( String sql, int pageNo, int pageSize );
}