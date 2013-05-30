package com.sanxing.statenet.matcher;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class AbstractStringMatcher
    implements StringMatcher
{
    private Set pattens;

    private boolean isUpperCase;

    public AbstractStringMatcher()
    {
        isUpperCase = true;
    }

    public void init()
    {
        pattens = new HashSet();
    }

    @Override
    public void addAll( List patters )
    {
        for ( int i = 0; i < patters.size(); ++i )
        {
            pattens.add( patters.get( i ) );
        }
    }

    @Override
    public void addPatten( String patter )
    {
        pattens.add( patter );
    }

    @Override
    public void addAll( Set patters )
    {
        Iterator it = patters.iterator();
        while ( it.hasNext() )
        {
            pattens.add( it.next() );
        }
    }

    @Override
    public void addAll( String[] patters )
    {
        for ( int i = 0; i < patters.length; ++i )
        {
            pattens.add( patters[i] );
        }
    }

    @Override
    public boolean removePatten( String patter )
    {
        if ( pattens.contains( patter ) )
        {
            pattens.remove( patter );
            return true;
        }
        return false;
    }

    @Override
    public void reset()
    {
        init();
    }

    @Override
    public int getPattenCount()
    {
        return pattens.size();
    }

    @Override
    public String getPatten( int index )
    {
        Object[] obj = pattens.toArray();
        return obj[index].toString();
    }

    @Override
    public void clearPattens()
    {
        init();
    }

    @Override
    public String[] getPattens()
    {
        Object[] temp = pattens.toArray();
        String[] tempPatters = new String[temp.length];
        for ( int i = 0; i < temp.length; ++i )
        {
            tempPatters[i] = temp[i].toString();
        }
        return tempPatters;
    }

    @Override
    public void setCaseSenitive( boolean b )
    {
        isUpperCase = b;
    }

    @Override
    public boolean isCaseSenitive()
    {
        return isUpperCase;
    }

    @Override
    public abstract boolean match( String paramString );

    @Override
    public abstract boolean matcher( String paramString, boolean paramBoolean );
}