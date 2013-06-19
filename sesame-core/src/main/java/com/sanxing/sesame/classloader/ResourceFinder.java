package com.sanxing.sesame.classloader;

import java.net.URL;
import java.util.Enumeration;

public interface ResourceFinder
{
    public abstract URL findResource( String resourceName );

    public abstract Enumeration findResources( String resourceName );

    public abstract ResourceHandle getResource( String resourceName );
}