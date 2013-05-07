package com.sanxing.sesame.logging.dao;

import com.sanxing.sesame.logging.monitor.SQLCondition;

public class LogQueryBean
    extends BaseBean
{
    private SQLCondition condition;

    public SQLCondition getCondition()
    {
        return condition;
    }

    public void setCondition( SQLCondition condition )
    {
        this.condition = condition;
    }

    public LogQueryBean( SQLCondition condition )
    {
        this.condition = condition;
    }
}