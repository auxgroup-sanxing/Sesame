package com.sanxing.sesame.jdbc.template;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract interface RowMapping
{
    public abstract Object handleRow( ResultSet paramResultSet )
        throws SQLException;
}