package com.sanxing.sesame.logging.impl;

import java.util.Hashtable;

import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;

public class LogFactoryImpl
    extends LogFactory
{
    protected Hashtable instances = new Hashtable();

    @Override
    public Log getInstance( String name )
    {
        Log instance = (Log) instances.get( name );
        if ( instance == null )
        {
            instance = new SesameLogger( name );
            instances.put( name, instance );
        }
        return instance;
    }
}