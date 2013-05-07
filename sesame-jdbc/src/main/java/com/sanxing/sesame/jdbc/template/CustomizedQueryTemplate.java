package com.sanxing.sesame.jdbc.template;

import java.util.List;
import java.util.Map;

public abstract interface CustomizedQueryTemplate
{
    public abstract Object queryRow( String paramString, ParametersMapping paramParametersMapping,
                                     IndexedRowMapping paramIndexedRowMapping );

    public abstract List<Object> query( String paramString, ParametersMapping paramParametersMapping,
                                        IndexedRowMapping paramIndexedRowMapping );

    public abstract List<Object> query( String paramString, ParametersMapping paramParametersMapping,
                                        IndexedRowMapping paramIndexedRowMapping, int paramInt1, int paramInt2 );

    public abstract Object queryRow( String paramString, ParametersMapping paramParametersMapping,
                                     RowMapping paramRowMapping );

    public abstract List<Object> query( String paramString, ParametersMapping paramParametersMapping,
                                        RowMapping paramRowMapping );

    public abstract List<Object> query( String paramString, ParametersMapping paramParametersMapping,
                                        RowMapping paramRowMapping, int paramInt1, int paramInt2 );

    public abstract <T> T queryRow( String paramString, ParametersMapping paramParametersMapping,
                                    IndexedRowMapping paramIndexedRowMapping, Class<T> paramClass );

    public abstract <T> List<T> query( String paramString, ParametersMapping paramParametersMapping,
                                       IndexedRowMapping paramIndexedRowMapping, Class<T> paramClass );

    public abstract <T> List<T> query( String paramString, ParametersMapping paramParametersMapping,
                                       IndexedRowMapping paramIndexedRowMapping, Class<T> paramClass, int paramInt1,
                                       int paramInt2 );

    public abstract <T> T queryRow( String paramString, ParametersMapping paramParametersMapping,
                                    RowMapping paramRowMapping, Class<T> paramClass );

    public abstract <T> List<T> query( String paramString, ParametersMapping paramParametersMapping,
                                       RowMapping paramRowMapping, Class<T> paramClass );

    public abstract <T> List<T> query( String paramString, ParametersMapping paramParametersMapping,
                                       RowMapping paramRowMapping, Class<T> paramClass, int paramInt1, int paramInt2 );

    public abstract Map<String, Object> queryRow( String paramString, ParametersMapping paramParametersMapping );

    public abstract List<Map<String, Object>> query( String paramString, ParametersMapping paramParametersMapping );

    public abstract List<Map<String, Object>> query( String paramString, ParametersMapping paramParametersMapping,
                                                     int paramInt1, int paramInt2 );
}