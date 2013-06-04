package com.sanxing.adp.api;

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
}