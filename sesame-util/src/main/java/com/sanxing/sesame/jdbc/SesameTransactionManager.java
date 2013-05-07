package com.sanxing.sesame.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameTransactionManager
    implements TransactionManager, UserTransaction
{
    private static Logger LOG = LoggerFactory.getLogger( SesameTransactionManager.class );

    private SesameDataSource ds = null;

    public static SesameTransactionManager getInstance()
    {
        return new SesameTransactionManager();
    }

    private static ThreadLocal<LinkedList<SesameTransaction>> getTransactions()
    {
        return SesameTransaction.getTransactions();
    }

    public static boolean isInTX()
    {
        if ( getTransactions().get() == null )
        {
            return false;
        }

        return ( ( (LinkedList) getTransactions().get() ).size() > 0 );
    }

    public void bindWithDataSource( DataSource ads )
    {
        SesameDataSource arterDataSource = (SesameDataSource) ads;
        ds = arterDataSource;
    }

    @Override
    public void begin()
        throws NotSupportedException, SystemException
    {
        if ( ds == null )
        {
            throw new NotSupportedException( "transaction manager is not bind with any datasource,call bind first" );
        }

        if ( ( getCurrentTX() != null ) && ( !( getCurrentTX().isSuspend() ) ) )
        {
            throw new NotSupportedException( "you must suspend the previous tx" );
        }

        if ( getTransactions().get() == null )
        {
            LinkedList txs = new LinkedList();
            getTransactions().set( txs );
        }

        SesameTransaction transaction = new SesameTransaction();
        Connection conn = null;
        try
        {
            conn = ds.getNewConnection();
            conn.setAutoCommit( false );
        }
        catch ( Throwable t )
        {
            try
            {
                if ( conn != null )
                {
                    conn.setAutoCommit( true );
                    conn.close();
                }
            }
            catch ( SQLException se )
            {
                LOG.error( se.getMessage(), se );
                throw new SystemException( se.getMessage() );
            }

            LOG.error( t.getMessage(), t );

            handleAtmFault( t );
        }
        transaction.bindConnection( conn );
        transaction.setIdx( ( (LinkedList) getTransactions().get() ).size() );
        ( (LinkedList) getTransactions().get() ).addLast( transaction );
    }

    private void handleAtmFault( Throwable t )
        throws SystemException
    {
        if ( t != null )
        {
            if ( t instanceof SQLException )
            {
                SQLException sqlEx = (SQLException) t;
                throw new SystemException( sqlEx.getMessage() );
            }
            if ( t instanceof Error )
            {
                Error err = (Error) t;
                throw err;
            }
            throw new SystemException( t.getMessage() );
        }

        throw new NullPointerException( "stm operation fault is null" );
    }

    @Override
    public void commit()
        throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException,
        SecurityException, SystemException
    {
        SesameTransaction tx = getCurrentTX();
        if ( tx.isSuspend() )
        {
            throw new IllegalStateException( "the tx is suspended" );
        }
        tx.commit();
    }

    @Override
    public void rollback()
        throws IllegalStateException, SecurityException, SystemException
    {
        SesameTransaction tx = getCurrentTX();
        if ( tx.isSuspend() )
        {
            throw new IllegalStateException( "the tx is suspended" );
        }
        tx.rollback();
    }

    @Override
    public void setRollbackOnly()
        throws IllegalStateException, SystemException
    {
        SesameTransaction tx = getCurrentTX();
        if ( tx.isSuspend() )
        {
            throw new SystemException( "the tx is suspended" );
        }
        tx.setRollbackOnly();
    }

    private static SesameTransaction getCurrentTX()
    {
        if ( getTransactions().get() == null )
        {
            return null;
        }

        if ( ( (LinkedList) getTransactions().get() ).isEmpty() )
        {
            return null;
        }

        SesameTransaction tx = (SesameTransaction) ( (LinkedList) getTransactions().get() ).getLast();
        if ( tx == null )
        {
            throw new IllegalStateException( "all tx in current tread is commited or rollbacked" );
        }
        return tx;
    }

    @Override
    public int getStatus()
        throws SystemException
    {
        if ( getTransactions() == null )
        {
            return 6;
        }

        if ( getTransactions().get() == null )
        {
            return 6;
        }

        if ( ( (LinkedList) getTransactions().get() ).isEmpty() )
        {
            return 6;
        }

        SesameTransaction tx = (SesameTransaction) ( (LinkedList) getTransactions().get() ).getLast();
        if ( tx == null )
        {
            return 6;
        }
        return tx.getStatus();
    }

    @Override
    public Transaction getTransaction()
        throws SystemException
    {
        return getCurrentTX();
    }

    public static Connection getConnectionInTX()
    {
        SesameTransaction atx = getCurrentTX();
        return atx.getConnection();
    }

    @Override
    public void setTransactionTimeout( int seconds )
        throws SystemException
    {
    }

    @Override
    public Transaction suspend()
        throws SystemException
    {
        SesameTransaction tx = getCurrentTX();
        if ( tx.isSuspend() )
        {
            throw new SystemException( "the tx is suspended" );
        }
        try
        {
            tx.suspend();
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
            throw new SystemException( e.getMessage() );
        }
        return tx;
    }

    @Override
    public void resume( Transaction tobj )
        throws IllegalStateException, InvalidTransactionException, SystemException
    {
        SesameTransaction curTX = getCurrentTX();
        SesameTransaction tx = (SesameTransaction) tobj;
        if ( curTX.getIdx() != tx.getIdx() )
        {
            throw new IllegalStateException( "there are nested tx is actvied" );
        }
        tx.resume();
    }

    public static void destory()
    {
        if ( getTransactions().get() != null )
        {
            getTransactions().set( null );
            getTransactions().remove();
        }
    }
}