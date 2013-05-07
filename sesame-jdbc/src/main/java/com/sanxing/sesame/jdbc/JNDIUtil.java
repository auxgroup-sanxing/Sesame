package com.sanxing.sesame.jdbc;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.sanxing.sesame.util.GetterUtil;
import com.sanxing.sesame.util.ServerDetector;

public class JNDIUtil
{
    private static boolean onSesame = false;

    private static final Logger LOG = LoggerFactory.getLogger( JNDIUtil.class );

    private static InitialContext ic;

    static
    {
        try
        {
            Class.forName( "com.sanxing.sesame.core.naming.JNDIUtil" );
            LOG.trace( "we are on sesame" );
            onSesame = true;
        }
        catch ( ClassNotFoundException e )
        {
            onSesame = false;
        }
    }

    public static InitialContext getInitialContext()
    {
        if ( ic != null )
        {
            return ic;
        }

        if ( onSesame )
        {
            try
            {
                LOG.trace( "ON SESAME" );
                Class clazz = Class.forName( "com.sanxing.sesame.core.naming.JNDIUtil" );
                Method method = clazz.getDeclaredMethod( "getInitialContext", new Class[0] );
                ic = (InitialContext) method.invoke( null, new Object[0] );
            }
            catch ( Exception e )
            {
                LOG.error( "got JNDI ctx from sesame err", e );
            }
        }
        else
        {
            if ( ( ServerDetector.isJetty() ) || ( ServerDetector.isTomcat() ) )
            {
                LOG.trace( "IN SMC............" );
            }
            try
            {
                return new InitialContext();
            }
            catch ( NamingException e )
            {
                e.printStackTrace();
                try
                {
                    LOG.trace( "IN UNIT TEST" );
                    LOG.trace( "create JNDI context for adp test" );
                    System.setProperty( "java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory" );
                    Hashtable env = new Hashtable();
                    env.put( "org.osjava.sj.jndi.shared", "true" );
                    ic = new InitialContext( env );
                    Properties props = new Properties();
                    props.load( JNDIUtil.class.getClassLoader().getResourceAsStream( "db.config" ) );
                    String uniqueName = "sesame.adp.test";
                    int maxPoolSize = GetterUtil.getInteger( props.getProperty( "max-pool-size" ), 10 );
                    String jndiName = GetterUtil.getString( props.getProperty( "jndi-name" ) );
                    PoolingDataSource debugDatasource = new PoolingDataSource();
                    debugDatasource.setClassName( "bitronix.tm.resource.jdbc.lrc.LrcXADataSource" );

                    debugDatasource.setAllowLocalTransactions( true );
                    debugDatasource.setUniqueName( uniqueName );
                    debugDatasource.setMaxPoolSize( maxPoolSize );

                    debugDatasource.getDriverProperties().load(
                        JNDIUtil.class.getClassLoader().getResourceAsStream( "driver.properties" ) );

                    debugDatasource.setAllowLocalTransactions( true );
                    ic.bind( jndiName, debugDatasource );
                    try
                    {
                        debugDatasource.getConnection();
                    }
                    catch ( Exception e1 )
                    {
                        LOG.error( e1.getMessage(), e1 );
                    }

                    BitronixTransactionManager btm = TransactionManagerServices.getTransactionManager();
                    UserTransaction ut = null;
                    try
                    {
                        ut = (UserTransaction) ic.lookup( "java:comp/UserTransaction" );
                    }
                    catch ( Exception localException1 )
                    {
                    }
                    if ( ut == null )
                    {
                        ic.bind( "java:comp/UserTransaction", btm );
                    }
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                    LOG.error( e.getMessage(), ex );
                }
            }
        }
        return ic;
    }
}