package com.sanxing.sesame.wssecurity.jaas;

import java.security.Principal;

public class MemberOfGroupPrincipal
    implements Principal
{
    private String groupname;

    public MemberOfGroupPrincipal( String groupname )
    {
        if ( groupname == null )
        {
            throw new NullPointerException( "groupname cannot be null" );
        }
        this.groupname = groupname;
    }

    public String getName()
    {
        return this.groupname;
    }

    public boolean equals( Object another )
    {
        if ( another == null )
            return false;
        if ( this == another )
            return true;
        if ( ( another instanceof MemberOfGroupPrincipal ) )
        {
            return getName().equals( ( (MemberOfGroupPrincipal) another ).getName() );
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