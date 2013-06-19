package com.sanxing.sesame.jdbc.template;

import java.util.List;
import java.util.Map;

public interface CustomizedQueryTemplate
{
    public abstract Object queryRow( String sql, ParametersMapping paramMapping, IndexedRowMapping indexedRowMapping );

    public abstract List<Object> query( String sql, ParametersMapping paramMapping,
                                        IndexedRowMapping indexedRowMapping );

    public abstract List<Object> query( String sql, ParametersMapping paramMapping,
                                        IndexedRowMapping indexedRowMapping, int pageNo, int pageSize );

    public abstract Object queryRow( String sql, ParametersMapping paramMapping,
                                     RowMapping rowMapping );

    public abstract List<Object> query( String sql, ParametersMapping paramMapping,
                                        RowMapping rowMapping );

    public abstract List<Object> query( String sql, ParametersMapping paramMapping,
                                        RowMapping rowMapping, int pageNo, int pageSize );

    public abstract <T> T queryRow( String sql, ParametersMapping paramMapping,
                                    IndexedRowMapping indexedRowMapping, Class<T> rowClazz );

    public abstract <T> List<T> query( String sql, ParametersMapping paramMapping,
                                       IndexedRowMapping indexedRowMapping, Class<T> rowClazz );

    public abstract <T> List<T> query( String sql, ParametersMapping paramMapping,
                                       IndexedRowMapping indexedRowMapping, Class<T> rowClazz, int pageNo,
                                       int pageSize );

    public abstract <T> T queryRow( String sql, ParametersMapping paramMapping,
                                    RowMapping rowMapping, Class<T> rowClazz );

    public abstract <T> List<T> query( String sql, ParametersMapping paramMapping,
                                       RowMapping rowMapping, Class<T> rowClazz );

    public abstract <T> List<T> query( String sql, ParametersMapping paramMapping,
                                       RowMapping rowMapping, Class<T> rowClazz, int pageNo, int pageSize );

    public abstract Map<String, Object> queryRow( String sql, ParametersMapping paramMapping );

    public abstract List<Map<String, Object>> query( String sql, ParametersMapping paramMapping );

    public abstract List<Map<String, Object>> query( String sql, ParametersMapping paramMapping,
                                                     int pageNo, int pageSize );
}