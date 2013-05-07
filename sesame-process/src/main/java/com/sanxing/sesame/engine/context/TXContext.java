package com.sanxing.sesame.engine.context;

import javax.transaction.TransactionManager;

public class TXContext
    extends TryCatchContext
{
    private TransactionManager tx;

    public TransactionManager getTx()
    {
        return tx;
    }

    public void setTx( TransactionManager tx )
    {
        this.tx = tx;
    }
}