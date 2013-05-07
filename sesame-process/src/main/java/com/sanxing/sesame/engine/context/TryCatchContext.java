package com.sanxing.sesame.engine.context;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TryCatchContext
    implements Comparable<TryCatchContext>
{
    private int levelIndex = 1;

    public static final Logger logger = LoggerFactory.getLogger( TryCatchContext.class );

    private boolean tring = true;

    private String[] catchableExceptions;

    private List<Element> ExceptionHandleFlow;

    private int index;

    private final List<TryCatchContext> childrenTCCs = new LinkedList();

    private TryCatchContext parentTCC;

    private DataContext messageContext;

    public int getLevelIndex()
    {
        return levelIndex;
    }

    public void setLevelIndex( int levelIndex )
    {
        this.levelIndex = levelIndex;
    }

    @Override
    public int compareTo( TryCatchContext o )
    {
        return ( index - o.index );
    }

    public void close()
    {
        Iterator iter = childrenTCCs.iterator();
        if ( messageContext != null )
        {
            messageContext.close();
        }
        while ( iter.hasNext() )
        {
            TryCatchContext tcc = (TryCatchContext) iter.next();
            tcc.close();
            iter.remove();
        }
    }

    public List<Element> getExceptionHandleFlow()
    {
        return ExceptionHandleFlow;
    }

    public void setExceptionHandleFlow( List<Element> exceptionHandleFlow )
    {
        ExceptionHandleFlow = exceptionHandleFlow;
    }

    public String[] getCatchableExceptions()
    {
        return catchableExceptions;
    }

    public void setCatchableExceptions( String[] catchableExceptions )
    {
        this.catchableExceptions = catchableExceptions;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex( int index )
    {
        this.index = index;
    }

    public TryCatchContext getParentTCC()
    {
        return parentTCC;
    }

    public void setParentTCC( TryCatchContext parentTCC )
    {
        this.parentTCC = parentTCC;
        levelIndex = ( parentTCC.getLevelIndex() * 10 + parentTCC.getChildren().size() );
    }

    public DataContext getMessageContext()
    {
        return messageContext;
    }

    public void setMessageContext( DataContext messageContext )
    {
        this.messageContext = messageContext;
    }

    public void addChild( TryCatchContext context )
    {
        childrenTCCs.add( context );
        context.setParentTCC( this );
    }

    public List<TryCatchContext> getChildren()
    {
        return childrenTCCs;
    }

    public void endTrying()
    {
        tring = false;
    }

    public boolean isTring()
    {
        return tring;
    }

    @Override
    public String toString()
    {
        return "TryCatchContext{index=" + index + ", levelIndex=" + levelIndex + '}';
    }
}