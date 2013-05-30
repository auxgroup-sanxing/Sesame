package com.sanxing.statenet.auth;

import java.security.Principal;

public class StatenetPrincipal
    implements Principal
{
    private final String name;

    private String passwd;

    private String fullname;

    private String level;

    private String description;

    private transient int hash;

    public StatenetPrincipal( String name )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "Name cannot be null" );
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

        StatenetPrincipal that = (StatenetPrincipal) o;

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

    public void setPasswd( String passwd )
    {
        this.passwd = passwd;
    }

    public String getPasswd()
    {
        return passwd;
    }

    public void setFullname( String fullname )
    {
        this.fullname = fullname;
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setLevel( String level )
    {
        this.level = level;
    }

    public String getLevel()
    {
        return level;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}