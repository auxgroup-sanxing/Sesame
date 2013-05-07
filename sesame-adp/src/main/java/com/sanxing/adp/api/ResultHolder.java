package com.sanxing.adp.api;

import java.util.Date;

public class ResultHolder<T>
{
    private T value;

    public void setValue( T value )
    {
        this.value = value;
    }

    public T getValue()
    {
        return this.value;
    }

    public static void main( String[] args )
    {
        ResultHolder holder = new ResultHolder();
        holder.setValue( new Date() );
    }
}