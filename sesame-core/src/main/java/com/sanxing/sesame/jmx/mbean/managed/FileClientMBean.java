package com.sanxing.sesame.jmx.mbean.managed;

public interface FileClientMBean
{
    public abstract String fetchFile( String fileName );

    public abstract String getDescription();
}