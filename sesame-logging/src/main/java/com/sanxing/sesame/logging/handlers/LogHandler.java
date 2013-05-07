package com.sanxing.sesame.logging.handlers;

import com.sanxing.sesame.logging.dao.LogBean;

public abstract interface LogHandler
{
    public abstract void handle( LogBean paramLogBean );
}