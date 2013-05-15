package com.sanxing.sesame.core.jdbc;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;

import org.apache.commons.dbcp.BasicDataSource;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.jdbc.SesameDataSource;
import com.sanxing.sesame.jdbc.SesameTransactionManager;

public class STMProvider
    implements DataSourceProvider
{
    private static final Logger LOG = LoggerFactory.getLogger( STMProvider.class );

    private SesameDataSource sesameDataSource;

    @Override
    public void provide( BaseServer server, DataSourceInfo dsInfo )
    {
        try
        {
            Element resourceEl = dsInfo.getAppInfo();
            String driver = resourceEl.getChildText( "driver-class" );
            String url = resourceEl.getChildText( "url" );
            BasicDataSource datasource = new BasicDataSource();
            datasource.setDriverClassName( driver );
            datasource.setUrl( url );
            datasource.setUsername( resourceEl.getChildText( "username" ) );
            datasource.setPassword( resourceEl.getChildText( "password" ) );
            datasource.setMaxActive( Integer.parseInt( resourceEl.getChildText( "max-active" ) ) );
            datasource.setMaxIdle( Integer.parseInt( resourceEl.getChildText( "max-idle" ) ) );
            datasource.setMaxWait( Long.parseLong( resourceEl.getChildText( "max-wait" ) ) );
            datasource.setInitialSize( Integer.parseInt( resourceEl.getChildText( "initial-size" ) ) );
            sesameDataSource = new SesameDataSource( datasource );
            Context context = server.getNamingContext();
            String name = dsInfo.getJndiName();
            context.bind( name, sesameDataSource );

            SesameTransactionManager tm = SesameTransactionManager.getInstance();
            tm.bindWithDataSource( sesameDataSource );
            try
            {
                context.bind( "java:comp/UserTransaction", tm );
            }
            catch ( NameAlreadyBoundException e )
            {
                context.rebind( "java:comp/UserTransaction", tm );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }

    @Override
    public void release()
    {
    }
}