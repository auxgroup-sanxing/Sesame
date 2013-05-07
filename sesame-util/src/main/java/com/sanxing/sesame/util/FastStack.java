package com.sanxing.sesame.util;

import java.util.ArrayList;

public class FastStack<T>
    extends ArrayList<T>
{
    public void push( T o )
    {
        add( o );
    }

    public T pop()
    {
        return remove( size() - 1 );
    }

    public boolean empty()
    {
        return ( size() == 0 );
    }

    public T peek()
    {
        return get( size() - 1 );
    }
}