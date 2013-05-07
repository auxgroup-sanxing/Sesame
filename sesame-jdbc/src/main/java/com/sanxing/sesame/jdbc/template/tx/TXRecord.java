package com.sanxing.sesame.jdbc.template.tx;

import javax.transaction.Transaction;

public class TXRecord
{
    public static final int JOIN = 0;

    public static final int NEW = 1;

    public static final int SUSPEND_NEW = 2;

    private int type = 0;

    private Transaction suspendedTX = null;

    public TXRecord( int type )
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    public void setType( int type )
    {
        this.type = type;
    }

    public Transaction getSuspendedTX()
    {
        return suspendedTX;
    }

    public void setSuspendedTX( Transaction suspendedTX )
    {
        this.suspendedTX = suspendedTX;
    }
}