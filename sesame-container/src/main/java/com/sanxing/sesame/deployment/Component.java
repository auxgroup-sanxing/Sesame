package com.sanxing.sesame.deployment;

public class Component
{
    private String type;

    private String componentClassLoaderDelegation;

    private String bootstrapClassLoaderDelegation;

    private Identification identification;

    private String componentClassName;

    private String description;

    private ClassPath componentClassPath;

    private String bootstrapClassName;

    private ClassPath bootstrapClassPath;

    private SharedLibraryList[] sharedLibraries;

    private InstallationDescriptorExtension descriptorExtension;

    public Component()
    {
        componentClassLoaderDelegation = "parent-first";
        bootstrapClassLoaderDelegation = "parent-first";
    }

    public boolean isServiceEngine()
    {
        return ( ( type != null ) && ( type.equals( "service-engine" ) ) );
    }

    public boolean isBindingComponent()
    {
        return ( ( type != null ) && ( type.equals( "binding-component" ) ) );
    }

    public boolean isComponentClassLoaderDelegationParentFirst()
    {
        return isParentFirst( componentClassLoaderDelegation );
    }

    public boolean isComponentClassLoaderDelegationSelfFirst()
    {
        return isSelfFirst( componentClassLoaderDelegation );
    }

    public boolean isBootstrapClassLoaderDelegationParentFirst()
    {
        return isParentFirst( bootstrapClassLoaderDelegation );
    }

    public boolean isBootstrapClassLoaderDelegationSelfFirst()
    {
        return isSelfFirst( bootstrapClassLoaderDelegation );
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getComponentClassLoaderDelegation()
    {
        return componentClassLoaderDelegation;
    }

    public void setComponentClassLoaderDelegation( String componentClassLoaderDelegation )
    {
        this.componentClassLoaderDelegation = componentClassLoaderDelegation;
    }

    public String getBootstrapClassLoaderDelegation()
    {
        return bootstrapClassLoaderDelegation;
    }

    public void setBootstrapClassLoaderDelegation( String bootstrapClassLoaderDelegation )
    {
        this.bootstrapClassLoaderDelegation = bootstrapClassLoaderDelegation;
    }

    public Identification getIdentification()
    {
        return identification;
    }

    public void setIdentification( Identification identification )
    {
        this.identification = identification;
    }

    public String getComponentClassName()
    {
        return componentClassName;
    }

    public void setComponentClassName( String componentClassName )
    {
        this.componentClassName = componentClassName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public ClassPath getComponentClassPath()
    {
        return componentClassPath;
    }

    public void setComponentClassPath( ClassPath componentClassPath )
    {
        this.componentClassPath = componentClassPath;
    }

    public String getBootstrapClassName()
    {
        return bootstrapClassName;
    }

    public void setBootstrapClassName( String bootstrapClassName )
    {
        this.bootstrapClassName = bootstrapClassName;
    }

    public ClassPath getBootstrapClassPath()
    {
        return bootstrapClassPath;
    }

    public void setBootstrapClassPath( ClassPath bootstrapClassPath )
    {
        this.bootstrapClassPath = bootstrapClassPath;
    }

    public SharedLibraryList[] getSharedLibraries()
    {
        return sharedLibraries;
    }

    public void setSharedLibraries( SharedLibraryList[] sharedLibraries )
    {
        this.sharedLibraries = sharedLibraries;
    }

    public InstallationDescriptorExtension getDescriptorExtension()
    {
        return descriptorExtension;
    }

    public void setDescriptorExtension( InstallationDescriptorExtension descriptorExtension )
    {
        this.descriptorExtension = descriptorExtension;
    }

    protected boolean isParentFirst( String text )
    {
        return ( ( text != null ) && ( text.equalsIgnoreCase( "parent-first" ) ) );
    }

    protected boolean isSelfFirst( String text )
    {
        return ( ( text != null ) && ( text.equalsIgnoreCase( "self-first" ) ) );
    }
}