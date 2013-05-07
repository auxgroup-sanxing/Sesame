package com.sanxing.sesame.engine.action.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCTM
    implements TXManager
{
    private Connection conn;

    private static Logger LOG = LoggerFactory.getLogger( JDBCTM.class );

    private int status = -1;

    public void setConnection( Connection conn )
    {
        this.conn = conn;
    }

    @Override
    public Connection getConnection()
    {
        if ( conn == null )
        {
            throw new RuntimeException( "tx not started" );
        }
        return conn;
    }

    @Override
    public void begin()
    {
        if ( status == -1 )
        {
            try
            {
                conn.setAutoCommit( false );
                status = 0;
            }
            catch ( SQLException e )
            {
                LOG.error( "Begin transaction failure", e );
            }
        }
        else if ( status == 0 )
        {
            LOG.warn( this + "already begin" );
        }
        else if ( status == 1 )
        {
            LOG.warn( this + " already commited" );
        }
        else if ( status == 2 )
        {
            LOG.warn( this + " already rollbacked" );
        }
    }

    @Override
    public void commit()
    {
        if ( status == 0 )
        {
            try
            {
                conn.commit();
            }
            catch ( SQLException e )
            {
                LOG.error( "commit failure", e );
            }
        }
        else if ( status == 1 )
        {
            LOG.warn( this + " already commited" );
        }
        else if ( status == 2 )
        {
            LOG.warn( this + " already rollbacked" );
        }
        else
        {
            LOG.warn( "unkown status" );
        }
    }

    @Override
    public void rollback()
    {
        if ( status == 0 )
        {
            try
            {
                conn.rollback();
            }
            catch ( SQLException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
        else if ( status == 1 )
        {
            LOG.warn( this + " already commited" );
        }
        else if ( status == 2 )
        {
            LOG.warn( this + " already rollbacked" );
        }
        else
        {
            LOG.warn( "unkown status" );
        }
    }

    @Override
    public int getStatus()
    {
        return status;
    }
}