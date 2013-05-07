package com.sanxing.sesame.jdbc.template.dialect.impl;

import com.sanxing.sesame.jdbc.data.PageInfo;
import com.sanxing.sesame.jdbc.template.dialect.DataAccessDialect;

public class OracleDialect
    implements DataAccessDialect
{
    private static OracleDialect instance = new OracleDialect();

    public static OracleDialect getInstance()
    {
        return instance;
    }

    @Override
    public String getPagedSql( String sql, int pageNo, int pageSize )
    {
        PageInfo.vlidatePageInfo( pageNo, pageSize );

        int startPos = ( pageNo - 1 ) * pageSize + 1;
        int endPos = startPos + pageSize - 1;
        StringBuilder pagingSelect = new StringBuilder( sql.length() + 100 );
        pagingSelect.append( "SELECT * FROM ( SELECT ROWNUM AS ROWNUM_ , ROW_.* FROM ( " );
        pagingSelect.append( sql );
        pagingSelect.append( " ) ROW_ ) WHERE ROWNUM_ <= " );
        pagingSelect.append( String.valueOf( endPos ) );
        pagingSelect.append( " AND ROWNUM_ >= " );
        pagingSelect.append( String.valueOf( startPos ) );
        return pagingSelect.toString();
    }
}