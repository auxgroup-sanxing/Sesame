package com.sanxing.sesame.logging.dao;

public class SesameCalloutLogDAO
    extends SesameLogDAO
{
    @Override
    public void setTableName()
    {
        setTableName( "sesame_callout_log" );
    }
}