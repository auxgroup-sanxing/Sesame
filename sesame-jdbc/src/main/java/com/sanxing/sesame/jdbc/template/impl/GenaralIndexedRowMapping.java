package com.sanxing.sesame.jdbc.template.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.sanxing.sesame.jdbc.template.IndexedRowMapping;
import com.sanxing.sesame.jdbc.template.RowMapping;

public class GenaralIndexedRowMapping
    implements IndexedRowMapping
{
    private RowMapping rowMapping = null;

    public GenaralIndexedRowMapping( RowMapping rowMapping )
    {
        this.rowMapping = rowMapping;
    }

    @Override
    public Object handleRow( ResultSet rs, int rowNo )
        throws SQLException
    {
        return rowMapping.handleRow( rs );
    }
}