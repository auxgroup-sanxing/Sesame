package com.sanxing.sesame.jdbc.template.dialect;

import java.util.HashMap;
import java.util.Map;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.template.dialect.impl.DB2Dialect;
import com.sanxing.sesame.jdbc.template.dialect.impl.OracleDialect;
import com.sanxing.sesame.util.SystemProperties;

public class DataAccessDialectManager
{
    private static String dbName = null;

    private static Map<String, DataAccessDialect> dbDialectMap = new HashMap();

    static
    {
        dbName = SystemProperties.get( "sesame.database", "Oracle" );

        dbDialectMap.put( "Oracle", OracleDialect.getInstance() );
        dbDialectMap.put( "DB2", DB2Dialect.getInstance() );
    }

    public static DataAccessDialect getDataAccessDialect()
    {
        DataAccessDialect dialect = dbDialectMap.get( dbName );
        if ( dialect == null )
        {
            String errMsg = "cannot support " + dbName + " database";
            throw new DataAccessException( errMsg );
        }
        return dialect;
    }
}