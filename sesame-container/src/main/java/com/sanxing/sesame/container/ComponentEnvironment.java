package com.sanxing.sesame.container;

import java.io.File;

import com.sanxing.sesame.mbean.ComponentMBeanImpl;

public class ComponentEnvironment
{
    private File installRoot;

    private File workspaceRoot;

    private File componentRoot;

    private File stateFile;

    private ComponentMBeanImpl localConnector;

    public File getInstallRoot()
    {
        return installRoot;
    }

    public void setInstallRoot( File installRoot )
    {
        this.installRoot = installRoot;
    }

    public File getWorkspaceRoot()
    {
        return workspaceRoot;
    }

    public void setWorkspaceRoot( File workspaceRoot )
    {
        this.workspaceRoot = workspaceRoot;
    }

    public ComponentMBeanImpl getLocalConnector()
    {
        return localConnector;
    }

    public void setLocalConnector( ComponentMBeanImpl localConnector )
    {
        this.localConnector = localConnector;
    }

    public File getComponentRoot()
    {
        return componentRoot;
    }

    public void setComponentRoot( File componentRoot )
    {
        this.componentRoot = componentRoot;
    }

    public File getStateFile()
    {
        return stateFile;
    }

    public void setStateFile( File stateFile )
    {
        this.stateFile = stateFile;
    }
}