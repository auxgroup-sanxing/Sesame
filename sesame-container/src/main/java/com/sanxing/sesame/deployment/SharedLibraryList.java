package com.sanxing.sesame.deployment;

public class SharedLibraryList
{
    private String version;

    private String name;

    public SharedLibraryList()
    {
    }

    public SharedLibraryList( String name )
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof SharedLibraryList ) )
        {
            return false;
        }

        SharedLibraryList sharedLibraryList = (SharedLibraryList) o;

        if ( name != null && name.equals( sharedLibraryList.name ) && version != null
            && version.equals( sharedLibraryList.version ) )
        {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int result = ( version != null ) ? version.hashCode() : 0;
        result = ( ( 29 * result ) + name != null ) ? name.hashCode() : 0;
        return result;
    }

    @Override
    public String toString()
    {
        return "SharedLibraryList[version=" + version + "; name=" + name + "]";
    }
}