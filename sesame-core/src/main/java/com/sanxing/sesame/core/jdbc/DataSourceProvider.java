package com.sanxing.sesame.core.jdbc;

import com.sanxing.sesame.core.BaseServer;

public interface DataSourceProvider
{
    public abstract void provide( BaseServer server, DataSourceInfo dsInfo );

    public abstract void release();
}