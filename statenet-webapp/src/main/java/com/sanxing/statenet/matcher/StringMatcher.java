package com.sanxing.statenet.matcher;

import java.util.List;
import java.util.Set;

public abstract interface StringMatcher
{
    public abstract void addPatten( String paramString );

    public abstract void addAll( List paramList );

    public abstract void addAll( Set paramSet );

    public abstract void addAll( String[] paramArrayOfString );

    public abstract boolean removePatten( String paramString );

    public abstract void reset();

    public abstract int getPattenCount();

    public abstract String getPatten( int paramInt );

    public abstract void clearPattens();

    public abstract String[] getPattens();

    public abstract void setCaseSenitive( boolean paramBoolean );

    public abstract boolean isCaseSenitive();

    public abstract boolean match( String paramString );

    public abstract boolean matcher( String paramString, boolean paramBoolean );
}