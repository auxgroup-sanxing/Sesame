package com.sanxing.sesame.jdbc.template;

public abstract interface IndexedUpdateTemplate
{
    public abstract int update( String paramString, Object[] paramArrayOfObject, int[] paramArrayOfInt );

    public abstract int update( String paramString, Object[] paramArrayOfObject );
}