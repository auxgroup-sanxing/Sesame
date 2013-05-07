package com.sanxing.sesame.deployment;

import java.util.Arrays;
import java.util.List;

public class ClassPath
{
    private String[] pathElements = new String[0];

    public ClassPath()
    {
    }

    public ClassPath( String[] pathElements )
    {
        this.pathElements = pathElements;
    }

    public String[] getPathElements()
    {
        return pathElements;
    }

    public void setPathElements( String[] pathElements )
    {
        this.pathElements = pathElements;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ClassPath ) )
        {
            return false;
        }

        ClassPath classPath = (ClassPath) o;

        return ( Arrays.equals( pathElements, classPath.pathElements ) );
    }

    @Override
    public int hashCode()
    {
        if ( pathElements == null )
        {
            return 0;
        }
        int result = 1;
        for ( int i = 0; i < pathElements.length; ++i )
        {
            result = 31 * result + ( ( pathElements[i] == null ) ? 0 : pathElements[i].hashCode() );
        }
        return result;
    }

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "ClassPath[" );
        for ( int i = 0; i < pathElements.length; ++i )
        {
            String pathElement = pathElements[i];
            if ( i > 0 )
            {
                buffer.append( ", " );
            }
            buffer.append( pathElement );
        }
        return buffer.toString();
    }

    public List getPathList()
    {
        return Arrays.asList( pathElements );
    }

    public void setPathList( List list )
    {
        pathElements = new String[list.size()];
        list.toArray( pathElements );
    }
}