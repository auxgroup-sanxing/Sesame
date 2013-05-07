package com.sanxing.sesame.jmx.security;

import java.security.Principal;

public class UserPrincipal
    implements Principal
{
    private final String name;

    private transient int hash;

    public UserPrincipal( String name )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "name cannot be null" );
        }
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( ( o == null ) || ( super.getClass() != o.getClass() ) )
        {
            return false;
        }

        UserPrincipal that = (UserPrincipal) o;

        return ( name.equals( that.name ) );
    }

    @Override
    public int hashCode()
    {
        if ( hash == 0 )
        {
            hash = name.hashCode();
        }
        return hash;
    }

    @Override
    public String toString()
    {
        return name;
    }
}