package com.sanxing.sesame.logging.handlers;

import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.monitor.StoppableMessageQueue;

public class MonitorQueueHandler
    implements LogHandler
{
    @Override
    public void handle( LogBean bean )
    {
        StoppableMessageQueue queue = StoppableMessageQueue.getInstance();
        queue.putMessage( bean );
    }
}