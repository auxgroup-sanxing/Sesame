package com.sanxing.sesame.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameDataSource
    implements DataSource
{
    private static Logger LOG = LoggerFactory.getLogger( SesameDataSource.class );

    private DataSource backend = null;

    public SesameDataSource( DataSource backend )
    {
        if ( backend != null )
        {
            if ( backend instanceof SesameDataSource )
            {
                throw new RuntimeException( "can not wrap sesameDataSource" );
            }
        }
        else
        {
            throw new NullPointerException( "data source is null" );
        }
        this.backend = backend;
    }

    @Override
    public Connection getConnection()
        throws SQLException
    {
        Connection conn = null;
        if ( SesameTransactionManager.isInTX() )
        {
            conn = SesameTransactionManager.getConnectionInTX();
            if ( LOG.isTraceEnabled() )
            {
                LOG.debug( "in transaction env. use old con [" + conn + "]" );
            }
        }
        else
        {
            conn = getNewConnection();
            if ( LOG.isTraceEnabled() )
            {
                LOG.debug( "create new con [" + conn + "]" );
            }
        }
        return conn;
    }

    @Override
    public Connection getConnection( String username, String password )
        throws SQLException
    {
        Connection conn = null;
        if ( SesameTransactionManager.isInTX() )
        {
            conn = SesameTransactionManager.getConnectionInTX();
        }
        else
        {
            conn = getNewConnection( username, password );
        }
        return conn;
    }

    public Connection getNewConnection()
        throws SQLException
    {
        Connection conn = new SesameConnection( backend.getConnection() );
        return conn;
    }

    public Connection getNewConnection( String username, String password )
        throws SQLException
    {
        Connection conn = new SesameConnection( backend.getConnection( username, password ) );
        return conn;
    }

    @Override
    public int getLoginTimeout()
        throws SQLException
    {
        return backend.getLoginTimeout();
    }

    @Override
    public PrintWriter getLogWriter()
        throws SQLException
    {
        return backend.getLogWriter();
    }

    @Override
    public void setLoginTimeout( int seconds )
        throws SQLException
    {
        backend.setLoginTimeout( seconds );
    }

    @Override
    public void setLogWriter( PrintWriter out )
        throws SQLException
    {
        backend.setLogWriter( out );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap( Class<T> iface )
        throws SQLException
    {
        return backend.unwrap( iface );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor( Class<?> iface )
        throws SQLException
    {
        return backend.isWrapperFor( iface );
    }
}