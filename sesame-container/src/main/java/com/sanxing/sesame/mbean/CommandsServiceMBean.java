package com.sanxing.sesame.mbean;

import java.util.Properties;

import javax.jbi.management.LifeCycleMBean;

public interface CommandsServiceMBean
    extends LifeCycleMBean
{
    public abstract String installComponent( String file, Properties props, boolean deferException )
        throws Exception;

    public abstract String uninstallComponent( String name )
        throws Exception;

    public abstract String installSharedLibrary( String file, boolean deferException )
        throws Exception;

    public abstract String uninstallSharedLibrary( String name )
        throws Exception;

    public abstract String startComponent( String name )
        throws Exception;

    public abstract String stopComponent( String name )
        throws Exception;

    public abstract String shutdownComponent( String name )
        throws Exception;

    public abstract String deployServiceAssembly( String file, boolean deferException )
        throws Exception;

    public abstract String undeployServiceAssembly( String name )
        throws Exception;

    public abstract String startServiceAssembly( String name )
        throws Exception;

    public abstract String stopServiceAssembly( String name )
        throws Exception;

    public abstract String shutdownServiceAssembly( String name )
        throws Exception;

    public abstract String installArchive( String location )
        throws Exception;

    public abstract String listComponents( boolean excludeSEs, boolean excludeBCs, boolean excludePojos, String requiredState,
                                           String sharedLibraryName, String serviceAssemblyName )
        throws Exception;

    public abstract String listSharedLibraries( String componentName, String sharedLibraryName )
        throws Exception;

    public abstract String listServiceAssemblies( String state, String componentName, String serviceAssemblyName )
        throws Exception;
}