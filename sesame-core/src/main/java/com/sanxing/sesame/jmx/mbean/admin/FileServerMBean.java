package com.sanxing.sesame.jmx.mbean.admin;

import com.sanxing.sesame.jmx.FilePackage;

public interface FileServerMBean
{
    public abstract FilePackage transfer( FilePackage filePackage );

    public abstract String getDescription();
}