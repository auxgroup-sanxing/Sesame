package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract interface TypeHandler
{
    public abstract void setParameter( PreparedStatement paramPreparedStatement, int paramInt, Object paramObject )
        throws SQLException;

    public abstract Object getField( ResultSet paramResultSet, int paramInt )
        throws SQLException;
}