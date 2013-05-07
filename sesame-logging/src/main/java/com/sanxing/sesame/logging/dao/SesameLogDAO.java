package com.sanxing.sesame.logging.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.jdbc.data.PageInfo;
import com.sanxing.sesame.jdbc.template.NamedUpdateTemplate;
import com.sanxing.sesame.jdbc.template.TemplateManager;

public class SesameLogDAO
    implements SesameBaseDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( SesameLogDAO.class );

    private String tableName;

    private String updateStateSQL;

    private String insertSQL;

    private String updateSQL;

    private String querySQL;

    public SesameLogDAO()
    {
        setTableName();

        String insertSQL =
            "insert into " + tableName + "(serialNumber,  startTime,  updateTime, state, stage, content,"
                + "serviceName, operationName, transactionCode, channel, exceptionMessage) " + "values "
                + "(#serialNumber#, #startTime#, #updateTime#, #state#, #stage#, #content#,"
                + "#serviceName#,  #operationName#,  #transactionCode#, #channel#, #exceptionMessage# )";
        setInsertSQL( insertSQL );

        String querySQL =
            "select serialNumber, serviceName, operationName, transactionCode, channel, startTime, updateTime, state, content, exceptionMessage from "
                + getTableName() + " where serialNumber = #serialNumber# ";
        setQuerySQL( querySQL );

        String updateStateSQL =
            "update " + getTableName()
                + " set updateTime = #updateTime#, state = #state#, exceptionMessage = #exceptionMessage#"
                + "where serialNumber = #serialNumber# ";
        setUpdateStateSQL( updateStateSQL );
    }

    public void setTableName()
    {
        setTableName( "sesame_log" );
    }

    public String getDataSourceName()
    {
        return System.getProperty( "sesame.logging.monitor.datasource.name", "STM_DATASOURCE" );
    }

    public String getUpdateStateSQL()
    {
        return updateStateSQL;
    }

    public void setUpdateStateSQL( String updateStateSQL )
    {
        this.updateStateSQL = updateStateSQL;
    }

    public String getUpdateSQL()
    {
        return updateSQL;
    }

    public void setUpdateSQL( String updateSQL )
    {
        this.updateSQL = updateSQL;
    }

    public String getInsertSQL()
    {
        return insertSQL;
    }

    public void setInsertSQL( String insertSQL )
    {
        this.insertSQL = insertSQL;
    }

    public String getQuerySQL()
    {
        return querySQL;
    }

    public void setQuerySQL( String querySQL )
    {
        this.querySQL = querySQL;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName( String tableName )
    {
        this.tableName = tableName;
    }

    @Override
    public boolean updateOnDuplicate( BaseBean bean )
    {
        boolean isDuplicate = false;
        if ( update( bean ) > 0 )
        {
            isDuplicate = true;
        }
        else
        {
            insert( bean );
        }
        return isDuplicate;
    }

    @Override
    public boolean updateStateOnDuplicate( BaseBean bean )
    {
        boolean isDuplicate = false;
        if ( updateState( bean ) > 0 )
        {
            isDuplicate = true;
        }
        else
        {
            insert( bean );
        }
        return isDuplicate;
    }

    @Override
    public void insert( BaseBean bean )
    {
        if ( !( bean instanceof LogBean ) )
        {
            return;
        }
        LogBean log = (LogBean) bean;
        LOG.debug( "insert " + log );
        NamedUpdateTemplate template = TemplateManager.getNamedUpdateTemplate( getDataSourceName() );
        template.update( getInsertSQL(), log );
    }

    @Override
    public int update( BaseBean bean )
    {
        LOG.debug( "update" );
        if ( !( bean instanceof LogBean ) )
        {
            return 0;
        }
        LogBean log = (LogBean) bean;
        LOG.debug( "update " + log );
        NamedUpdateTemplate template = TemplateManager.getNamedUpdateTemplate( getDataSourceName() );
        int rows = template.update( getUpdateSQL(), log );
        return rows;
    }

    @Override
    public int updateState( BaseBean bean )
    {
        LOG.debug( "update state" );
        if ( !( bean instanceof LogBean ) )
        {
            return 0;
        }
        LogBean log = (LogBean) bean;
        LOG.debug( "updateState " + log );
        NamedUpdateTemplate template = TemplateManager.getNamedUpdateTemplate( getDataSourceName() );
        int rows = template.update( getUpdateStateSQL(), log );
        return rows;
    }

    @Override
    public LogBean queryForRecord( BaseBean bean )
    {
        return null;
    }

    @Override
    public List<?> queryForRecordSet( BaseBean bean, PageInfo pageInfo )
    {
        return null;
    }

    @Override
    public long queryCount( BaseBean bean )
    {
        return 0L;
    }
}