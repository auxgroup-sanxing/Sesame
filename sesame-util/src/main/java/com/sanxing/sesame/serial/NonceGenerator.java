package com.sanxing.sesame.serial;

public class NonceGenerator
    extends SerialGenerator
{
    @Override
    public long allocate()
    {
        return 1L;
    }

    @Override
    public long getLimit()
    {
        return 9223372036854775807L;
    }
}