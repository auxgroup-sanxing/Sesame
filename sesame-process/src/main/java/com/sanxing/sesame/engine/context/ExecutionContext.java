package com.sanxing.sesame.engine.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.TransactionManager;

import bsh.EvalError;
import bsh.Interpreter;

import com.sanxing.sesame.engine.action.beanshell.BeanShellContext;
import com.sanxing.sesame.engine.action.callout.Reverter;
import com.sanxing.sesame.engine.action.jdbc.ConnectionUtil;

public class ExecutionContext
{
    public static final int STATUS_NORMAL = 0;

    public static final int STATUS_ERRORHANDLING = 1;

    private int status;

    private final List<TransactionManager> beginedTMS = new LinkedList();

    private final Reverter reverter = new Reverter();

    private final String uuid;

    private final DataContext dataCtx;

    private boolean doCutpoint = false;

    private final Map<String, Object> context = new HashMap();

    private TransactionManager currentTM;

    private final AtomicBoolean debugging = new AtomicBoolean( false );

    private final ArrayBlockingQueue<String> actQueue = new ArrayBlockingQueue( 1 );

    private final Interpreter bsh;

    private boolean terminated = false;

    public String getCurrentAction()
        throws InterruptedException
    {
        return actQueue.take();
    }

    public void setCurrentAction( String currentAction )
        throws InterruptedException
    {
        actQueue.put( currentAction );
    }

    public ExecutionContext( String uuid )
    {
        this.uuid = uuid;
        dataCtx = DataContext.getInstance( uuid );
        dataCtx.setExecutionContext( this );
        bsh = new Interpreter();
    }

    public TransactionManager getCurrentTM()
    {
        return currentTM;
    }

    public void setCurrentTM( TransactionManager currentTM )
    {
        beginedTMS.add( currentTM );
        this.currentTM = currentTM;
    }

    public Interpreter getBshInterpreter()
    {
        return bsh;
    }

    public void close()
    {
        dataCtx.close();
        ConnectionUtil.clean( uuid );
        actQueue.clear();
        BeanShellContext bsc = (BeanShellContext) get( "beanshell.context" );
        if ( bsc == null )
        {
            return;
        }
        try
        {
            bsc.close();
        }
        catch ( EvalError e )
        {
            e.printStackTrace();
        }
    }

    public Reverter getReverter()
    {
        return reverter;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void put( String key, Object value )
    {
        context.put( key, value );
    }

    public Object get( String key )
    {
        return context.get( key );
    }

    public boolean isDoCutpoint()
    {
        return doCutpoint;
    }

    public void setDoCutpoint( boolean doCutpoint )
    {
        this.doCutpoint = doCutpoint;
    }

    public DataContext getDataContext()
    {
        return dataCtx;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus( int status )
    {
        this.status = status;
    }

    public boolean isDebugging()
    {
        return debugging.get();
    }

    public void openDebugging()
    {
        debugging.set( true );
    }

    public void closeDebugging()
    {
        debugging.set( false );
    }

    public boolean isDehydrated()
    {
        return DehydrateManager.isDehydrated( getUuid() );
    }

    public void terminate()
    {
        terminated = true;
    }

    public boolean isTerminated()
    {
        return terminated;
    }

    public static void main( String[] args )
    {
        for ( int i = 0; i < 1000; ++i )
        {
            for ( int j = 0; j < 10; ++j )
            {
                new Interpreter();
            }
        }
    }
}