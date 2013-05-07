package com.sanxing.sesame.core.jdbc;

import java.sql.SQLException;

import javax.naming.Context;

import org.apache.commons.dbcp.BasicDataSource;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.BaseServer;

public class DBCPProvider
    implements DataSourceProvider
{
    private static Logger LOG = LoggerFactory.getLogger( DBCPProvider.class );

    private BasicDataSource datasource;

    @Override
    public void provide( BaseServer server, DataSourceInfo dsInfo )
    {
        try
        {
            Element resourceEl = dsInfo.getAppInfo();

            String driver = resourceEl.getChildText( "driver-class" );
            String url = resourceEl.getChildText( "url" );

            datasource = new BasicDataSource();

            datasource.setDriverClassName( driver );
            datasource.setUrl( url );
            datasource.setUsername( resourceEl.getChildText( "username" ) );
            datasource.setPassword( resourceEl.getChildText( "password" ) );
            datasource.setMaxActive( Integer.parseInt( resourceEl.getChildText( "max-active" ) ) );
            datasource.setMaxIdle( Integer.parseInt( resourceEl.getChildText( "max-idle" ) ) );
            datasource.setMaxWait( Long.parseLong( resourceEl.getChildText( "max-wait" ) ) );
            datasource.setInitialSize( Integer.parseInt( resourceEl.getChildText( "initial-size" ) ) );
            Context context = server.getNamingContext();
            String name = dsInfo.getJndiName();

            context.bind( name, datasource );
        }
        catch ( Exception e )
        {
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( e.getMessage(), e );
            }
            else
            {
                LOG.error( e.getMessage() );
            }
        }
    }

    @Override
    public void release()
    {
        try
        {
            datasource.close();
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    public static void main( String[] args )
    {
        BasicDataSource datasource = new BasicDataSource();

        datasource.setDriverClassName( "" );
        datasource.setUrl( "" );
        datasource.setUsername( "db2inst1" );
        datasource.setPassword( "db2inst1" );
    }
}