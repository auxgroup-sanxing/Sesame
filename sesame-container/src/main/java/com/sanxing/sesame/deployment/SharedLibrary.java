package com.sanxing.sesame.deployment;

public class SharedLibrary
{
    private String classLoaderDelegation = "parent-first";

    private String version;

    private Identification identification;

    private ClassPath sharedLibraryClassPath;

    private String callbackClazz;

    public String getClassLoaderDelegation()
    {
        return classLoaderDelegation;
    }

    public void setClassLoaderDelegation( String classLoaderDelegation )
    {
        this.classLoaderDelegation = classLoaderDelegation;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public Identification getIdentification()
    {
        return identification;
    }

    public void setIdentification( Identification identification )
    {
        this.identification = identification;
    }

    public ClassPath getSharedLibraryClassPath()
    {
        return sharedLibraryClassPath;
    }

    public void setSharedLibraryClassPath( ClassPath sharedLibraryClassPath )
    {
        this.sharedLibraryClassPath = sharedLibraryClassPath;
    }

    public boolean isParentFirstClassLoaderDelegation()
    {
        return ( ( classLoaderDelegation != null ) && ( classLoaderDelegation.equalsIgnoreCase( "parent-first" ) ) );
    }

    public boolean isSelfFirstClassLoaderDelegation()
    {
        return ( ( classLoaderDelegation != null ) && ( classLoaderDelegation.equalsIgnoreCase( "self-first" ) ) );
    }

    public String getCallbackClazz()
    {
        return callbackClazz;
    }

    public void setCallbackClazz( String callbackClazz )
    {
        this.callbackClazz = callbackClazz;
    }
}