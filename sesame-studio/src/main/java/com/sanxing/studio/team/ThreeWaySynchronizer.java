package com.sanxing.studio.team;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public interface ThreeWaySynchronizer
{
    public abstract long put( File localPath, String dstPath, String comment )
        throws SCMException;

    public abstract long checkout( String path, File localPath, long revision )
        throws SCMException;

    public abstract long commit( File[] wcPaths, String comment )
        throws SCMException;

    public abstract void cleanup( File localPath )
        throws SCMException;

    public abstract void add( File[] wcPaths, boolean recursive )
        throws SCMException;

    public abstract void delete( File localPath )
        throws SCMException;

    public abstract Collection<?> getChangeList( File localPath )
        throws SCMException;

    public abstract void move( File src, File dst )
        throws SCMException;

    public abstract void lock( File wcPath, String comment )
        throws SCMException;

    public abstract void unlock( File wcPath, boolean breakLock )
        throws SCMException;

    public abstract long update( File wcPath, long revision )
        throws SCMException;

    public abstract long synchronize( File wcPath )
        throws SCMException;

    public abstract void relocate( File wcRoot, String path )
        throws SCMException;

    public abstract void revert( File wcPath )
        throws SCMException;

    public abstract void resolve( File wcPath, String choice )
        throws SCMException;

    public abstract void disconnect( File wcPath )
        throws SCMException;

    public abstract Map<String, ?> info( File wcPath )
        throws SCMException;

    public abstract Map<String, ?> status( File wcPath )
        throws SCMException;

    public abstract boolean isIgnored( File wcPath );

    public abstract boolean isVersioned( File wcPath );

    public abstract void dispose();
}