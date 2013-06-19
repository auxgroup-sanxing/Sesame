package com.sanxing.sesame.jdbc.template.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler
{
    public abstract void setParameter( PreparedStatement ps, int index, Object value )
        throws SQLException;

    public abstract Object getField( ResultSet rs, int index )
        throws SQLException;
}