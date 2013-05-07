package com.sanxing.sesame.engine.action.flow;

import org.jdom.Element;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;

public class ForkAction
    extends AbstractAction
{
    Element config;

    @Override
    public void doinit( Element config )
    {
        this.config = config;
    }

    @Override
    public void dowork( DataContext ctx )
    {
    }

    public static void main( String[] args )
    {
        try
        {
            Runnable run = new Runnable()
            {
                @Override
                public void run()
                {
                    System.out.println( "hello" );
                }
            };
            Thread worker = new Thread( run );
            worker.start();
            Thread.currentThread();
            Thread.sleep( 1000L );
            worker.join();
            System.out.println( "ok" );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }
}