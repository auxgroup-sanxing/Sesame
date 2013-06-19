package com.sanxing.sesame.jdbc.template;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapping
{
    public abstract Object handleRow( ResultSet rs )
        throws SQLException;
}