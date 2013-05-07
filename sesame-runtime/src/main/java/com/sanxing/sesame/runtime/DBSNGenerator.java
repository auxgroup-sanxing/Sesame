package com.sanxing.sesame.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sanxing.sesame.core.naming.JNDIUtil;
import com.sanxing.sesame.serial.SerialGenerator;

public class DBSNGenerator
    extends SerialGenerator
{
    private final AtomicLong limit = new AtomicLong();

    private final DataSource ds;

    private static String DATASOURCE_NAME = "STM_DATASOURCE";

    private static String QUERY_SERIAL_SQL = " SELECT CUR_SERIAL FROM SESAME_SYSCTL FOR UPDATE ";

    private static String UPDATE_SERIAL_SQL = " UPDATE SESAME_SYSCTL SET CUR_SERIAL = ? ";

    public DBSNGenerator()
        throws NamingException
    {
        InitialContext namingContext = JNDIUtil.getInitialContext();
        ds = ( (DataSource) namingContext.lookup( DATASOURCE_NAME ) );
        if ( ds == null )
        {
            throw new RuntimeException( "DataSource not found: [" + DATASOURCE_NAME + "]" );
        }
    }

    @Override
    public long getLimit()
    {
        return limit.get();
    }

    @Override
    public long allocate()
    {
        try
        {
            Connection conn = ds.getConnection();
            try
            {
                long sn = 1L;
                conn.setAutoCommit( false );
                PreparedStatement selectStmt = conn.prepareStatement( QUERY_SERIAL_SQL );
                PreparedStatement updateStmt = conn.prepareStatement( UPDATE_SERIAL_SQL );

                ResultSet rs = selectStmt.executeQuery();
                if ( rs.next() )
                {
                    sn = rs.getLong( 1 );
                    updateStmt.setLong( 1, sn + 1000L );
                    updateStmt.execute();
                }
                else
                {
                    throw new Exception( "In the table [SESAME_SYSCTL] , CUR_SERIAL is not initialize!" );
                }
                rs.close();
                conn.commit();

                limit.set( sn + 1000L );
                return sn;
            }
            catch ( SQLException e )
            {
                throw e;
            }
            finally
            {
                conn.close();
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }
}