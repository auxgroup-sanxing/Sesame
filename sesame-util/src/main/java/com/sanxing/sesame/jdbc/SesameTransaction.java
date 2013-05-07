package com.sanxing.sesame.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameTransaction
    implements Transaction
{
    private static Logger LOG = LoggerFactory.getLogger( SesameTransaction.class );

    private int status = 0;

    private boolean isSuspend = false;

    private int idx = 0;

    private SesameConnection jdbcCon = null;

    private static ThreadLocal<LinkedList<SesameTransaction>> transactions = new ThreadLocal();

    static ThreadLocal<LinkedList<SesameTransaction>> getTransactions()
    {
        return transactions;
    }

    public Connection getConnection()
    {
        return jdbcCon;
    }

    public void bindConnection( Connection con )
    {
        jdbcCon = ( (SesameConnection) con );
    }

    public void suspend()
        throws SQLException
    {
        isSuspend = true;
    }

    public boolean isSuspend()
    {
        return isSuspend;
    }

    public int getIdx()
    {
        return idx;
    }

    public void setIdx( int idx )
    {
        this.idx = idx;
    }

    public void resume()
    {
        isSuspend = false;
    }

    private void reallyCloseConn()
    {
        try
        {
            jdbcCon.setAutoCommit( true );
            jdbcCon.reallyClose();
        }
        catch ( Throwable t )
        {
            try
            {
                LOG.error( t.getMessage(), t );
            }
            catch ( Throwable localThrowable1 )
            {
            }
        }
    }

    @Override
    public void commit()
        throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException,
        SystemException
    {
        if ( LOG.isTraceEnabled() )
        {
            LOG.trace( "TX [" + this + "] commit...." );
        }

        if ( isSuspend )
        {
            throw new SystemException( "tx is suspended, resume it first" );
        }

        if ( status == 1 )
        {
            rollback();
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( "TX [" + this + "] rollback" );
            }
            throw new RollbackException( "transaction is rollbacked" );
        }
        try
        {
            status = 8;
            jdbcCon.commit();
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( "TX [" + this + "] commited" );
            }
            status = 3;
            reallyCloseConn();
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
            throw new SystemException( e.getMessage() );
        }
        ( (LinkedList) transactions.get() ).removeLast();
    }

    @Override
    public void rollback()
        throws IllegalStateException, SystemException
    {
        if ( LOG.isTraceEnabled() )
        {
            LOG.trace( "TX [" + this + "] rollback...." );
        }

        if ( canRollback() )
        {
            if ( isSuspend )
            {
                throw new IllegalStateException( "tx is suspended, resume it first" );
            }
            try
            {
                status = 9;
                jdbcCon.rollback();
                if ( LOG.isTraceEnabled() )
                {
                    LOG.trace( "TX [" + this + "] rollbacked...." );
                }
                status = 4;
                reallyCloseConn();
            }
            catch ( SQLException e )
            {
                LOG.error( e.getMessage(), e );
                throw new SystemException( e.getMessage() );
            }
            finally
            {
                ( (LinkedList) transactions.get() ).removeLast();
            }
        }
    }

    boolean canRollback()
    {
        return ( ( status != 3 ) && ( status != 9 ) && ( status != 4 ) );
    }

    @Override
    public boolean delistResource( XAResource xaRes, int flag )
        throws IllegalStateException, SystemException
    {
        return false;
    }

    @Override
    public boolean enlistResource( XAResource xaRes )
        throws IllegalStateException, RollbackException, SystemException
    {
        return false;
    }

    @Override
    public int getStatus()
        throws SystemException
    {
        return status;
    }

    @Override
    public void registerSynchronization( Synchronization synch )
        throws IllegalStateException, RollbackException, SystemException
    {
    }

    @Override
    public void setRollbackOnly()
        throws IllegalStateException, SystemException
    {
        status = 1;
    }

    @Override
    public String toString()
    {
        return "sesameTransaction [status=" + status + ", isSuspend=" + isSuspend + ", idx=" + idx + ", jdbcCon="
            + jdbcCon + "]";
    }
}