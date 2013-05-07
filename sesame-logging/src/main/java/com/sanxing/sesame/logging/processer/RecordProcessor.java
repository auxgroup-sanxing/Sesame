package com.sanxing.sesame.logging.processer;

import java.util.ArrayList;
import java.util.List;

import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.handlers.LogHandler;

public abstract class RecordProcessor
    implements Processor
{
    protected List<LogHandler> handlers = new ArrayList();

    protected LogBean bean;

    @Override
    public void process( Object o )
    {
        register();

        bean = parse( o );

        for ( Object element : handlers )
        {
            LogHandler handler = (LogHandler) element;
            handler.handle( bean );
        }
    }

    public abstract void register();

    public abstract LogBean parse( Object paramObject );
}