package com.sanxing.sesame.engine.action.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;

public class ConnectionUtil
{
    static Map<String, List<Connection>> openedConnections = new HashMap();

    public static Connection getEngineDBConnection( String uuid )
    {
        Connection con = makeNewConnection();
        if ( openedConnections.get( uuid ) == null )
        {
            openedConnections.put( uuid, new LinkedList() );
        }
        List opendConnectionsUnderExecutionContext = openedConnections.get( uuid );
        opendConnectionsUnderExecutionContext.add( con );
        return con;
    }

    public static void clean( String uuid )
    {
        List opendConnectionsUnderExecutionContext = openedConnections.get( uuid );
        if ( opendConnectionsUnderExecutionContext != null )
        {
            Iterator iterator = opendConnectionsUnderExecutionContext.iterator();
            while ( iterator.hasNext() )
            {
                Connection con = (Connection) iterator.next();
                try
                {
                    con.close();
                }
                catch ( SQLException e )
                {
                    e.printStackTrace();
                }

                iterator.remove();
            }
        }
    }

    private static Connection makeNewConnection()
    {
        BasicDataSource datasource = null;

        datasource = new BasicDataSource();
        datasource.setDriverClassName( "com.mysql.jdbc.Driver" );
        datasource.setUrl( "jdbc:mysql://localhost:3306/mydb" );
        datasource.setUsername( "root" );
        datasource.setPassword( "" );
        try
        {
            return datasource.getConnection();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }
}