package com.sanxing.sesame.logging.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.jdbc.data.PageInfo;
import com.sanxing.sesame.jdbc.template.NamedQueryTemplate;
import com.sanxing.sesame.jdbc.template.TemplateManager;

public class LogQueryDAO
    implements SesameBaseDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( LogQueryDAO.class );

    private String querySQL;

    private String tableName = "sesame_log";

    @Override
    public LogBean queryForRecord( BaseBean bean )
    {
        if ( !( bean instanceof LogQueryBean ) )
        {
            return null;
        }
        LogQueryBean queryBean = (LogQueryBean) bean;
        String selectClause = "select state, stage, exceptionMessage, content from " + getTableName() + " ";
        String orderClause = " order by updatetime ";
        querySQL = selectClause + queryBean.getCondition().getCondition() + orderClause;

        LOG.debug( "The query SQL= " + querySQL );
        NamedQueryTemplate template = TemplateManager.getNamedQueryTemplate( getDataSourceName() );
        List logBeans = template.query( querySQL, null, LogBean.class );
        LogBean user = null;
        if ( logBeans.size() > 0 )
        {
            user = (LogBean) logBeans.get( 0 );
        }

        return user;
    }

    @Override
    public List<?> queryForRecordSet( BaseBean bean, PageInfo pageInfo )
    {
        if ( !( bean instanceof LogQueryBean ) )
        {
            return null;
        }
        LogQueryBean queryBean = (LogQueryBean) bean;
        String selectClause =
            "select serialNumber, serviceName, operationName, transactionCode, channel, startTime, updateTime, state, stage, content from "
                + getTableName() + " ";
        String orderClause = " order by updatetime ";
        querySQL = selectClause + queryBean.getCondition().getCondition() + orderClause;
        LOG.debug( "The query SQL= " + querySQL );

        NamedQueryTemplate template = TemplateManager.getNamedQueryTemplate( getDataSourceName() );
        List logBeans =
            template.query( querySQL, null, LogBean.class, pageInfo.getCurrentPageNo(), pageInfo.getPageSize() );
        return logBeans;
    }

    @Override
    public long queryCount( BaseBean bean )
    {
        if ( !( bean instanceof LogQueryBean ) )
        {
            return 0L;
        }
        LogQueryBean queryBean = (LogQueryBean) bean;
        String selectClause = "select count(serialNumber) as count from " + getTableName() + " ";
        querySQL = selectClause + queryBean.getCondition().getCondition();
        LOG.debug( "The queryCount SQL= " + querySQL );

        NamedQueryTemplate template = TemplateManager.getNamedQueryTemplate( getDataSourceName() );
        List logBeans = template.query( querySQL, null, LogBean.class );
        LogBean logBean = null;
        if ( logBeans.size() > 0 )
        {
            logBean = (LogBean) logBeans.get( 0 );
        }
        else
        {
            throw new NullPointerException( "no record" );
        }
        return logBean.getCount().longValue();
    }

    public String getDataSourceName()
    {
        return System.getProperty( "sesame.logging.monitor.datasource.name", "STM_DATASOURCE" );
    }

    @Override
    public void insert( BaseBean bean )
    {
    }

    @Override
    public int update( BaseBean bean )
    {
        return 0;
    }

    @Override
    public boolean updateOnDuplicate( BaseBean bean )
    {
        return false;
    }

    @Override
    public int updateState( BaseBean bean )
    {
        return 0;
    }

    @Override
    public boolean updateStateOnDuplicate( BaseBean bean )
    {
        return false;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName( String tableName )
    {
        this.tableName = tableName;
    }
}