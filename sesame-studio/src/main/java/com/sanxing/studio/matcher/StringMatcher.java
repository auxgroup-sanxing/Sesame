package com.sanxing.studio.matcher;

import java.util.List;
import java.util.Set;

public interface StringMatcher
{
    public abstract void addPatten( String patter );

    public abstract void addAll( List patters );

    public abstract void addAll( Set patters );

    public abstract void addAll( String[] patters );

    public abstract boolean removePatten( String patter );

    public abstract void reset();

    public abstract int getPattenCount();

    public abstract String getPatten( int index );

    public abstract void clearPattens();

    public abstract String[] getPattens();

    public abstract void setCaseSenitive( boolean caseSenitive );

    public abstract boolean isCaseSenitive();

    public abstract boolean match( String s );

    public abstract boolean matcher( String s, boolean caseSenitive );
}