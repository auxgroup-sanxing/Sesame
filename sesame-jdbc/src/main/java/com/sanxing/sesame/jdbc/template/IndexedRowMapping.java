package com.sanxing.sesame.jdbc.template;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IndexedRowMapping
{
    public abstract Object handleRow( ResultSet rs, int rowNo )
        throws SQLException;
}