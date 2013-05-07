package com.sanxing.sesame.container;

import java.io.File;

import com.sanxing.sesame.util.FileUtil;

public class ServiceAssemblyEnvironment
{
    private File rootDir;

    private File installDir;

    private File susDir;

    private File stateFile;

    public File getInstallDir()
    {
        return installDir;
    }

    public void setInstallDir( File installRoot )
    {
        installDir = installRoot;
    }

    public File getSusDir()
    {
        return susDir;
    }

    public void setSusDir( File susRoot )
    {
        susDir = susRoot;
    }

    public File getStateFile()
    {
        return stateFile;
    }

    public void setStateFile( File stateFile )
    {
        this.stateFile = stateFile;
    }

    public File getRootDir()
    {
        return rootDir;
    }

    public void setRootDir( File rootDir )
    {
        this.rootDir = rootDir;
    }

    public File getServiceUnitDirectory( String componentName, String suName )
    {
        File compDir = FileUtil.getDirectoryPath( susDir, componentName );
        return FileUtil.getDirectoryPath( compDir, suName );
    }
}