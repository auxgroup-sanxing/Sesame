package com.sanxing.sesame.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameConnection
    implements Connection
{
    private static Logger LOG = LoggerFactory.getLogger( SesameConnection.class );

    private Connection con = null;

    public SesameConnection( Connection con )
    {
        this.con = con;
    }

    @Override
    public void clearWarnings()
        throws SQLException
    {
        con.clearWarnings();
    }

    @Override
    public void close()
        throws SQLException
    {
        if ( !( SesameTransactionManager.isInTX() ) )
        {
            con.close();
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( " connection [" + this + "] closed" );
            }
        }
    }

    void reallyClose()
        throws SQLException
    {
        con.close();
        if ( LOG.isTraceEnabled() )
        {
            LOG.trace( " connection [" + this + "] closed" );
        }
    }

    @Override
    public void commit()
        throws SQLException
    {
        con.commit();
    }

    @Override
    public Statement createStatement()
        throws SQLException
    {
        return con.createStatement();
    }

    @Override
    public Statement createStatement( int resultSetType, int resultSetConcurrency, int resultSetHoldability )
        throws SQLException
    {
        return con.createStatement( resultSetType, resultSetConcurrency, resultSetHoldability );
    }

    @Override
    public Statement createStatement( int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
        return con.createStatement( resultSetType, resultSetConcurrency );
    }

    @Override
    public boolean getAutoCommit()
        throws SQLException
    {
        return con.getAutoCommit();
    }

    @Override
    public String getCatalog()
        throws SQLException
    {
        return con.getCatalog();
    }

    @Override
    public int getHoldability()
        throws SQLException
    {
        return con.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData()
        throws SQLException
    {
        return con.getMetaData();
    }

    @Override
    public int getTransactionIsolation()
        throws SQLException
    {
        return con.getTransactionIsolation();
    }

    @Override
    public Map<String, Class<?>> getTypeMap()
        throws SQLException
    {
        return con.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings()
        throws SQLException
    {
        return con.getWarnings();
    }

    @Override
    public boolean isClosed()
        throws SQLException
    {
        return con.isClosed();
    }

    @Override
    public boolean isReadOnly()
        throws SQLException
    {
        return con.isReadOnly();
    }

    @Override
    public String nativeSQL( String sql )
        throws SQLException
    {
        return con.nativeSQL( sql );
    }

    @Override
    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency,
                                          int resultSetHoldability )
        throws SQLException
    {
        return con.prepareCall( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
    }

    @Override
    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
        return con.prepareCall( sql, resultSetType, resultSetConcurrency );
    }

    @Override
    public CallableStatement prepareCall( String sql )
        throws SQLException
    {
        return con.prepareCall( sql );
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency,
                                               int resultSetHoldability )
        throws SQLException
    {
        return con.prepareStatement( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
        return con.prepareStatement( sql, resultSetType, resultSetConcurrency );
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys )
        throws SQLException
    {
        return con.prepareStatement( sql, autoGeneratedKeys );
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int[] columnIndexes )
        throws SQLException
    {
        return con.prepareStatement( sql, columnIndexes );
    }

    @Override
    public PreparedStatement prepareStatement( String sql, String[] columnNames )
        throws SQLException
    {
        return con.prepareStatement( sql, columnNames );
    }

    @Override
    public PreparedStatement prepareStatement( String sql )
        throws SQLException
    {
        return con.prepareStatement( sql );
    }

    @Override
    public void releaseSavepoint( Savepoint savepoint )
        throws SQLException
    {
        con.releaseSavepoint( savepoint );
    }

    @Override
    public void rollback()
        throws SQLException
    {
        con.rollback();
    }

    @Override
    public void rollback( Savepoint savepoint )
        throws SQLException
    {
        con.rollback( savepoint );
    }

    @Override
    public void setAutoCommit( boolean autoCommit )
        throws SQLException
    {
        con.setAutoCommit( autoCommit );
    }

    @Override
    public void setCatalog( String catalog )
        throws SQLException
    {
        con.setCatalog( catalog );
    }

    @Override
    public void setHoldability( int holdability )
        throws SQLException
    {
        con.setHoldability( holdability );
    }

    @Override
    public void setReadOnly( boolean readOnly )
        throws SQLException
    {
        con.setReadOnly( readOnly );
    }

    @Override
    public Savepoint setSavepoint()
        throws SQLException
    {
        return con.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint( String name )
        throws SQLException
    {
        return con.setSavepoint( name );
    }

    @Override
    public void setTransactionIsolation( int level )
        throws SQLException
    {
        con.setTransactionIsolation( level );
    }

    @Override
    public void setTypeMap( Map<String, Class<?>> map )
        throws SQLException
    {
        con.setTypeMap( map );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap( Class<T> iface )
        throws SQLException
    {
        return con.unwrap( iface );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor( Class<?> iface )
        throws SQLException
    {
        return con.isWrapperFor( iface );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#createClob()
     */
    @Override
    public Clob createClob()
        throws SQLException
    {
        return con.createClob();
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#createBlob()
     */
    @Override
    public Blob createBlob()
        throws SQLException
    {
        return con.createBlob();
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#createNClob()
     */
    @Override
    public NClob createNClob()
        throws SQLException
    {
        return con.createNClob();
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#createSQLXML()
     */
    @Override
    public SQLXML createSQLXML()
        throws SQLException
    {
        return con.createSQLXML();
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#isValid(int)
     */
    @Override
    public boolean isValid( int timeout )
        throws SQLException
    {
        return con.isValid( timeout );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
     */
    @Override
    public void setClientInfo( String name, String value )
        throws SQLClientInfoException
    {
        con.setClientInfo( name, value );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#setClientInfo(java.util.Properties)
     */
    @Override
    public void setClientInfo( Properties properties )
        throws SQLClientInfoException
    {
        con.setClientInfo( properties );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#getClientInfo(java.lang.String)
     */
    @Override
    public String getClientInfo( String name )
        throws SQLException
    {
        return con.getClientInfo( name );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#getClientInfo()
     */
    @Override
    public Properties getClientInfo()
        throws SQLException
    {
        return con.getClientInfo();
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
     */
    @Override
    public Array createArrayOf( String typeName, Object[] elements )
        throws SQLException
    {
        return con.createArrayOf( typeName, elements );
    }

    /*
     * (non-Javadoc)
     * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
     */
    @Override
    public Struct createStruct( String typeName, Object[] attributes )
        throws SQLException
    {
        return con.createStruct( typeName, attributes );
    }
}