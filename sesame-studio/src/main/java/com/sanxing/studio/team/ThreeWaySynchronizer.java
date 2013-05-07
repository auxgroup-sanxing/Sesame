package com.sanxing.studio.team;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public abstract interface ThreeWaySynchronizer
{
    public abstract long put( File paramFile, String paramString1, String paramString2 )
        throws SCMException;

    public abstract long checkout( String paramString, File paramFile, long paramLong )
        throws SCMException;

    public abstract long commit( File[] paramArrayOfFile, String paramString )
        throws SCMException;

    public abstract void cleanup( File paramFile )
        throws SCMException;

    public abstract void add( File[] paramArrayOfFile, boolean paramBoolean )
        throws SCMException;

    public abstract void delete( File paramFile )
        throws SCMException;

    public abstract Collection<?> getChangeList( File paramFile )
        throws SCMException;

    public abstract void move( File paramFile1, File paramFile2 )
        throws SCMException;

    public abstract void lock( File paramFile, String paramString )
        throws SCMException;

    public abstract void unlock( File paramFile, boolean paramBoolean )
        throws SCMException;

    public abstract long update( File paramFile, long paramLong )
        throws SCMException;

    public abstract long synchronize( File paramFile )
        throws SCMException;

    public abstract void relocate( File paramFile, String paramString )
        throws SCMException;

    public abstract void revert( File paramFile )
        throws SCMException;

    public abstract void resolve( File paramFile, String paramString )
        throws SCMException;

    public abstract void disconnect( File paramFile )
        throws SCMException;

    public abstract Map<String, ?> info( File paramFile )
        throws SCMException;

    public abstract Map<String, ?> status( File paramFile )
        throws SCMException;

    public abstract boolean isIgnored( File paramFile );

    public abstract boolean isVersioned( File paramFile );

    public abstract void dispose();
}