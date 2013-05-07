package com.sanxing.sesame.jdbc.template.impl;

import java.util.List;
import java.util.Map;

import com.sanxing.sesame.jdbc.template.DataAccessTemplate;
import com.sanxing.sesame.jdbc.template.IndexedRowMapping;
import com.sanxing.sesame.jdbc.template.ParametersMapping;
import com.sanxing.sesame.jdbc.template.RowMapping;
import com.sanxing.sesame.jdbc.template.dialect.DataAccessDialect;

public class DataAccessTemplateImpl
    extends BaseTemplateImpl
    implements DataAccessTemplate
{
    DataAccessTemplateImpl( String dsName )
    {
        super( dsName );
    }

    @Override
    public Map<String, Object> queryRow( String sql, Object[] parameters, int[] paramTypes )
    {
        List rowList = query( sql, parameters, paramTypes );
        validateSize( rowList.size() );
        if ( rowList.size() == 0 )
        {
            return null;
        }
        return ( (Map) rowList.get( 0 ) );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Object[] parameters, int[] paramTypes )
    {
        ParametersMapping paramMapping = new ArrayParametersMapping( parameters, paramTypes );
        return query( sql, paramMapping );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Object[] parameters, int[] paramTypes, int pageNo, int pageSize )
    {
        DataAccessDialect dialect = getDataAccessDialect();
        String pagedSql = dialect.getPagedSql( sql, pageNo, pageSize );
        return query( pagedSql, parameters, paramTypes );
    }

    @Override
    public Map<String, Object> queryRow( String sql, Object[] parameters )
    {
        int[] paramTypes = null;
        return queryRow( sql, parameters, paramTypes );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Object[] parameters )
    {
        int[] paramTypes = null;
        return query( sql, parameters, paramTypes );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Object[] parameters, int pageNo, int pageSize )
    {
        int[] paramTypes = null;
        return query( sql, parameters, paramTypes, pageNo, pageSize );
    }

    @Override
    public int update( String sql, Object[] parameters, int[] paramTypes )
    {
        ParametersMapping paramMapping = new ArrayParametersMapping( parameters, paramTypes );
        return update( sql, paramMapping );
    }

    @Override
    public int update( String sql, Object[] parameters )
    {
        int[] paramTypes = null;
        return update( sql, parameters, paramTypes );
    }

    @Override
    public Map<String, Object> queryRow( String sql, Map<String, Object> parameters, Map<String, Integer> paramTypes )
    {
        List rowList = query( sql, parameters, paramTypes );
        validateSize( rowList.size() );
        if ( rowList.size() == 0 )
        {
            return null;
        }
        return ( (Map) rowList.get( 0 ) );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Map<String, Object> parameters, Map<String, Integer> paramTypes )
    {
        ParametersMapping paramMapping = null;
        String realSql = null;
        if ( parameters != null )
        {
            InputInfo iInfo = TemplateHelper.getInputInfo( sql );
            realSql = iInfo.getSql();
            String[] paramNames = iInfo.getParamNames();
            paramMapping = new MapParametersMapping( parameters, paramNames, paramTypes );
        }
        else
        {
            realSql = sql;
        }
        return query( realSql, paramMapping );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Map<String, Object> parameters,
                                            Map<String, Integer> paramTypes, int pageNo, int pageSize )
    {
        DataAccessDialect dialect = getDataAccessDialect();
        String pagedSql = dialect.getPagedSql( sql, pageNo, pageSize );
        return query( pagedSql, parameters, paramTypes );
    }

    @Override
    public Map<String, Object> queryRow( String sql, Map<String, Object> parameters )
    {
        Map paramTypes = null;
        return queryRow( sql, parameters, paramTypes );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Map<String, Object> parameters )
    {
        Map paramTypes = null;
        return query( sql, parameters, paramTypes );
    }

    @Override
    public List<Map<String, Object>> query( String sql, Map<String, Object> parameters, int pageNo, int pageSize )
    {
        Map paramTypes = null;
        return query( sql, parameters, paramTypes, pageNo, pageSize );
    }

    @Override
    public <T> T queryRow( String sql, Object parameters, Class<T> rowClazz )
    {
        List<T> rowList = query( sql, parameters, rowClazz );
        validateSize( rowList.size() );
        if ( rowList.size() == 0 )
        {
            return null;
        }
        return rowList.get( 0 );
    }

    @Override
    public <T> List<T> query( String sql, Object parameters, Class<T> rowClazz )
    {
        ParametersMapping paramMapping = null;
        String realSql = null;
        if ( parameters != null )
        {
            InputInfo iInfo = TemplateHelper.getInputInfo( sql );
            realSql = iInfo.getSql();
            String[] paramNames = iInfo.getParamNames();
            paramMapping = new BeanParametersMapping( parameters, paramNames );
        }
        else
        {
            realSql = sql;
        }
        RowMapping rowMapping = new BeanRowMapping( rowClazz );
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        List retList = exeQuery( realSql, paramMapping, indexedRowMapping );
        return retList;
    }

    @Override
    public <T> List<T> query( String sql, Object parameters, Class<T> rowClazz, int pageNo, int pageSize )
    {
        DataAccessDialect dialect = getDataAccessDialect();
        String pagedSql = dialect.getPagedSql( sql, pageNo, pageSize );
        return query( pagedSql, parameters, rowClazz );
    }

    @Override
    public int update( String sql, Map<String, Object> parameters, Map<String, Integer> paramTypes )
    {
        ParametersMapping paramMapping = null;
        String realSql = null;
        if ( parameters != null )
        {
            InputInfo iInfo = TemplateHelper.getInputInfo( sql );
            realSql = iInfo.getSql();
            String[] paramNames = iInfo.getParamNames();
            paramMapping = new MapParametersMapping( parameters, paramNames, paramTypes );
        }
        else
        {
            realSql = sql;
        }
        return update( realSql, paramMapping );
    }

    @Override
    public int update( String sql, Map<String, Object> parameters )
    {
        Map paramTypes = null;
        return update( sql, parameters, paramTypes );
    }

    @Override
    public int update( String sql, Object parameters )
    {
        ParametersMapping paramMapping = null;
        String realSql = null;
        if ( parameters != null )
        {
            InputInfo iInfo = TemplateHelper.getInputInfo( sql );
            realSql = iInfo.getSql();
            String[] paramNames = iInfo.getParamNames();
            paramMapping = new BeanParametersMapping( parameters, paramNames );
        }
        else
        {
            realSql = sql;
        }
        return update( realSql, paramMapping );
    }

    @Override
    public Object queryRow( String sql, ParametersMapping paramMapping, IndexedRowMapping indexedRowMapping )
    {
        List rowList = query( sql, paramMapping, indexedRowMapping );
        validateSize( rowList.size() );
        if ( rowList.size() == 0 )
        {
            return null;
        }
        return rowList.get( 0 );
    }

    @Override
    public List<Object> query( String sql, ParametersMapping paramMapping, IndexedRowMapping indexedRowMapping )
    {
        List retList = exeQuery( sql, paramMapping, indexedRowMapping );
        return retList;
    }

    @Override
    public List<Object> query( String sql, ParametersMapping paramMapping, IndexedRowMapping indexedRowMapping,
                               int pageNo, int pageSize )
    {
        DataAccessDialect dialect = getDataAccessDialect();
        String pagedSql = dialect.getPagedSql( sql, pageNo, pageSize );
        return query( pagedSql, paramMapping, indexedRowMapping );
    }

    @Override
    public Object queryRow( String sql, ParametersMapping paramMapping, RowMapping rowMapping )
    {
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        return queryRow( sql, paramMapping, indexedRowMapping );
    }

    @Override
    public List<Object> query( String sql, ParametersMapping paramMapping, RowMapping rowMapping )
    {
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        return query( sql, paramMapping, indexedRowMapping );
    }

    @Override
    public List<Object> query( String sql, ParametersMapping paramMapping, RowMapping rowMapping, int pageNo,
                               int pageSize )
    {
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        return query( sql, paramMapping, indexedRowMapping, pageNo, pageSize );
    }

    @Override
    public <T> T queryRow( String sql, ParametersMapping paramMapping, IndexedRowMapping indexedRowMapping,
                           Class<T> rowClazz )
    {
        List<T> rowList = query( sql, paramMapping, indexedRowMapping, rowClazz );
        validateSize( rowList.size() );
        if ( rowList.size() == 0 )
        {
            return null;
        }
        return rowList.get( 0 );
    }

    @Override
    public <T> List<T> query( String sql, ParametersMapping paramMapping, IndexedRowMapping indexedRowMapping,
                              Class<T> rowClazz )
    {
        List retList = exeQuery( sql, paramMapping, indexedRowMapping );
        return retList;
    }

    @Override
    public <T> List<T> query( String sql, ParametersMapping paramMapping, IndexedRowMapping indexedRowMapping,
                              Class<T> rowClazz, int pageNo, int pageSize )
    {
        DataAccessDialect dialect = getDataAccessDialect();
        String pagedSql = dialect.getPagedSql( sql, pageNo, pageSize );
        return query( pagedSql, paramMapping, indexedRowMapping, rowClazz );
    }

    @Override
    public <T> T queryRow( String sql, ParametersMapping paramMapping, RowMapping rowMapping, Class<T> rowClazz )
    {
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        return queryRow( sql, paramMapping, indexedRowMapping, rowClazz );
    }

    @Override
    public <T> List<T> query( String sql, ParametersMapping paramMapping, RowMapping rowMapping, Class<T> rowClazz )
    {
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        return query( sql, paramMapping, indexedRowMapping, rowClazz );
    }

    @Override
    public <T> List<T> query( String sql, ParametersMapping paramMapping, RowMapping rowMapping, Class<T> rowClazz,
                              int pageNo, int pageSize )
    {
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        return query( sql, paramMapping, indexedRowMapping, rowClazz, pageNo, pageSize );
    }

    @Override
    public Map<String, Object> queryRow( String sql, ParametersMapping paramMapping )
    {
        List rowList = query( sql, paramMapping );
        validateSize( rowList.size() );
        if ( rowList.size() == 0 )
        {
            return null;
        }
        return ( (Map) rowList.get( 0 ) );
    }

    @Override
    public List<Map<String, Object>> query( String sql, ParametersMapping paramMapping )
    {
        RowMapping rowMapping = MapRowMapping.getInstance();
        IndexedRowMapping indexedRowMapping = new GenaralIndexedRowMapping( rowMapping );
        List retList = exeQuery( sql, paramMapping, indexedRowMapping );
        return retList;
    }

    @Override
    public List<Map<String, Object>> query( String sql, ParametersMapping paramMapping, int pageNo, int pageSize )
    {
        DataAccessDialect dialect = getDataAccessDialect();
        String pagedSql = dialect.getPagedSql( sql, pageNo, pageSize );
        return query( pagedSql, paramMapping );
    }

    @Override
    public int update( String sql, ParametersMapping paramMapping )
    {
        return exeUpdate( sql, paramMapping );
    }
}