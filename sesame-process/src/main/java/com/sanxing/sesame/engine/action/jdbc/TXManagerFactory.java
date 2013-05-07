package com.sanxing.sesame.engine.action.jdbc;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

public class TXManagerFactory
{
    public static TransactionManager getTxManager( Context namingContext )
        throws NamingException
    {
        TransactionManager tm = (TransactionManager) namingContext.lookup( "java:comp/UserTransaction" );
        return tm;
    }
}