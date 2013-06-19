package com.sanxing.sesame.jdbc.template;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParametersMapping
{
    public abstract void setParameters( PreparedStatement ps )
        throws SQLException;
}