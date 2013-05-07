package com.sanxing.sesame.classloader;

import java.net.URL;

public abstract class AbstractUrlResourceLocation
    implements ResourceLocation
{
    private final URL codeSource;

    public AbstractUrlResourceLocation( URL codeSource )
    {
        this.codeSource = codeSource;
    }

    @Override
    public final URL getCodeSource()
    {
        return codeSource;
    }

    @Override
    public void close()
    {
    }

    @Override
    public final boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( ( o == null ) || ( super.getClass() != o.getClass() ) )
        {
            return false;
        }

        AbstractUrlResourceLocation that = (AbstractUrlResourceLocation) o;
        return codeSource.equals( that.codeSource );
    }

    @Override
    public final int hashCode()
    {
        return codeSource.hashCode();
    }

    @Override
    public final String toString()
    {
        return "[" + super.getClass().getName() + ": " + codeSource + "]";
    }
}