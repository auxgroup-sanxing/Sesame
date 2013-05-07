package com.sanxing.sesame.engine.action.jdbc;

import java.sql.Connection;

public abstract interface TXManager
{
    public static final int BEGIN = 0;

    public static final int COMMITED = 1;

    public static final int ROLLBACK = 2;

    public abstract void begin();

    public abstract void commit();

    public abstract void rollback();

    public abstract Connection getConnection();

    public abstract int getStatus();
}