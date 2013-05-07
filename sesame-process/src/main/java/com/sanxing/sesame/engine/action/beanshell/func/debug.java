package com.sanxing.sesame.engine.action.beanshell.func;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.CallStack;
import bsh.EvalError;
import bsh.Interpreter;

import com.sanxing.sesame.exceptions.SystemException;

public class debug
{
    static Logger LOG = LoggerFactory.getLogger( "com.sanxing.sesame.engine.action.beanshell" );

    public static void invoke( Interpreter env, CallStack callstack, String message )
        throws EvalError
    {
        try
        {
            LOG.info( message );
        }
        catch ( Exception e )
        {
            env.set( "error", new SystemException() );
        }
    }

    public static void invoke( Interpreter env, CallStack callstack, Object message )
    {
        LOG.info( message.toString() );
    }
}