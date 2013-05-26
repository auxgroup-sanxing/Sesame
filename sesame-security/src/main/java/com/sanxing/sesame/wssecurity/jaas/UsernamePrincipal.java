package com.sanxing.sesame.wssecurity.jaas;

import java.security.Principal;

public class UsernamePrincipal
    implements Principal
{
    private String username;

    public UsernamePrincipal( String username )
    {
        if ( username == null )
        {
            throw new NullPointerException( "username cannot be null" );
        }
        this.username = username;
    }

    public String getName()
    {
        return this.username;
    }

    public boolean equals( Object another )
    {
        if ( another == null )
            return false;
        if ( this == another )
            return true;
        if ( ( another instanceof UsernamePrincipal ) )
        {
            return getName().equals( ( (UsernamePrincipal) another ).getName() );
        }
        return false;
    }

    public int hashCode()
    {
        return getName().hashCode();
    }

    public String toString()
    {
        return getClass().getName() + ": " + getName();
    }
}