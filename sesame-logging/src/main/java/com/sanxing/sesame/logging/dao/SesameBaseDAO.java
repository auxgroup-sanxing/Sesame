package com.sanxing.sesame.logging.dao;

import java.util.List;

import com.sanxing.sesame.jdbc.data.PageInfo;

public interface SesameBaseDAO
{
    public abstract boolean updateOnDuplicate( BaseBean bean );

    public abstract boolean updateStateOnDuplicate( BaseBean bean );

    public abstract int updateState( BaseBean bean );

    public abstract void insert( BaseBean bean );

    public abstract int update( BaseBean bean );

    public abstract LogBean queryForRecord( BaseBean bean );

    public abstract List<?> queryForRecordSet( BaseBean bean, PageInfo pageInfo );

    public abstract long queryCount( BaseBean bean );
}