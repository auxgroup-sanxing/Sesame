package com.sanxing.studio.team.svn;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.internal.wc.SVNStatusEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.sanxing.studio.team.SCM;
import com.sanxing.studio.team.SCMException;
import com.sanxing.studio.team.ThreeWaySynchronizer;
import com.sanxing.studio.utils.FileUtil;

public class SVNSynchronizer
    implements ThreeWaySynchronizer
{
    private static Logger LOG = LoggerFactory.getLogger( ThreeWaySynchronizer.class );

    private SVNClientManager clientManager;

    private String name;

    private String password;

    private String url;

    private SVNURL repositoryURL;

    private String lastInfo;

    private ISVNAuthenticationManager authManager;

    public SVNSynchronizer( String repositoryURL, String username, String password )
    {
        setName( username );
        setPassword( password );
        setRepositoryURL( repositoryURL );
        init();
    }

    @Override
    public Collection<?> getChangeList( File wcPath )
        throws SCMException
    {
        final Map hashmap = new HashMap();

        SVNStatusClient client = clientManager.getStatusClient();
        try
        {
            File wcRoot = SVNWCUtil.getWorkingCopyRoot( wcPath, true );
            if ( !( wcPath.equals( wcRoot ) ) )
            {
                File parentFile = wcPath.getParentFile();
                while ( parentFile != null )
                {
                    if ( parentFile.equals( wcRoot ) )
                    {
                        break;
                    }

                    if ( SVNWCUtil.isVersionedDirectory( parentFile ) )
                    {
                        wcRoot = SVNWCUtil.getWorkingCopyRoot( parentFile, true );
                        SVNStatus status = client.doStatus( parentFile, false, false );
                        if ( status.getNodeStatus() != SVNStatusType.STATUS_NORMAL )
                        {
                            Map properties = new HashMap();
                            properties.put( "path", status.getFile() );
                            properties.put( "status", Character.valueOf( status.getNodeStatus().getCode() ) );
                            properties.put( "prop", "" );
                            hashmap.put( parentFile, properties );
                        }
                    }
                    else
                    {
                        Map properties = new HashMap();
                        properties.put( "path", parentFile );
                        properties.put( "status", "?" );
                        properties.put( "prop", "" );
                        hashmap.put( parentFile, properties );
                    }
                    parentFile = parentFile.getParentFile();
                }

            }

            ISVNStatusHandler handler = new ISVNStatusHandler()
            {
                @Override
                public void handleStatus( SVNStatus status )
                    throws SVNException
                {
                    Map properties = new HashMap();
                    properties.put( "path", status.getFile() );
                    properties.put( "status", Character.valueOf( status.getNodeStatus().getCode() ) );
                    properties.put( "prop", null );
                    hashmap.put( status.getFile(), properties );
                    if ( status.getNodeStatus() == SVNStatusType.STATUS_CONFLICTED )
                    {
                        hashmap.remove( status.getConflictNewFile() );
                        hashmap.remove( status.getConflictOldFile() );
                        hashmap.remove( status.getConflictWrkFile() );
                    }
                    if ( ( status.getFile().isDirectory() )
                        && ( status.getNodeStatus() == SVNStatusType.STATUS_UNVERSIONED ) )
                    {
                        SVNSynchronizer.this.recursiveAdd( status.getFile(), hashmap );
                    }
                }
            };
            if ( SVNWCUtil.isVersionedDirectory( wcPath ) )
            {
                client.doStatus( wcPath, SVNRevision.UNDEFINED, SVNDepth.INFINITY, false, false, false, false, handler,
                    null );
            }
            else
            {
                Map properties = new HashMap();
                properties.put( "path", wcPath );
                properties.put( "status", "?" );
                properties.put( "prop", null );
                hashmap.put( wcPath, properties );
                if ( wcPath.isDirectory() )
                {
                    recursiveAdd( wcPath, hashmap );
                }
            }
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }

        return hashmap.values();
    }

    public SVNLock[] getLocks( String projectName )
        throws Exception
    {
        SVNRepository repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url ) );
        repository.setAuthenticationManager( authManager );
        return repository.getLocks( projectName );
    }

    @Override
    public long put( File localPath, String dstPath, String comment )
        throws SCMException
    {
        try
        {
            LOG.debug( "Import: " + localPath );
            if ( SVNWCUtil.isWorkingCopyRoot( localPath ) )
            {
                SVNStatus status = clientManager.getStatusClient().doStatus( localPath, true );

                if ( status.getURL().toDecodedString().equals( url ) )
                {
                    throw new SCMException( "文件已存在于版本库中", null );
                }

                disconnect( localPath );
            }

            SVNURL dstURL = repositoryURL.appendPath( dstPath, false );

            SVNProperties properties = new SVNProperties();

            clientManager.getCommitClient().doImport( localPath, dstURL, comment, properties, true, true,
                SVNDepth.EMPTY );

            return checkout( dstPath, localPath, 0L );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void disconnect( File wcPath )
        throws SCMException
    {
        LOG.debug( "Disconnect: " + wcPath );
        removeSVNMeta( wcPath );
    }

    private void recursiveAdd( File path, Map<File, Object> hashmap )
    {
        File[] entries = path.listFiles();
        Collection globalIgnores = SVNStatusEditor.getGlobalIgnores( clientManager.getOptions() );
        for ( File entry : entries )
        {
            if ( !( SVNStatusEditor.isIgnored( globalIgnores, entry ) ) )
            {
                Map properties = new HashMap();
                properties.put( "path", entry );
                properties.put( "status", Character.valueOf( '?' ) );
                properties.put( "prop", "" );
                hashmap.put( entry, properties );
                if ( entry.isDirectory() )
                {
                    recursiveAdd( entry, hashmap );
                }
            }
        }
    }

    private void removeSVNMeta( File wcPath )
    {
        File[] entries = wcPath.listFiles();
        for ( File entry : entries )
        {
            if ( entry.getName().equals( ".svn" ) )
            {
                FileUtil.deleteFile( entry );
            }
            else if ( entry.isDirectory() )
            {
                removeSVNMeta( entry );
            }
        }
    }

    @Override
    public void add( File[] wcPaths, boolean recursive )
        throws SCMException
    {
        try
        {
            Collection globalIgnores = SVNStatusEditor.getGlobalIgnores( clientManager.getOptions() );
            for ( File file : wcPaths )
            {
                if ( SVNStatusEditor.isIgnored( globalIgnores, file ) )
                {
                    continue;
                }

                clientManager.getWCClient().doAdd( file, true, false, true, SVNDepth.fromRecurse( recursive ), false,
                    true );
            }
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public long checkout( String path, File localPath, long revision )
        throws SCMException
    {
        try
        {
            SVNURL url = repositoryURL.appendPath( path, false );

            SVNUpdateClient updateClient = clientManager.getUpdateClient();

            updateClient.setIgnoreExternals( true );

            SVNRevision rev = ( revision == 0L ) ? SVNRevision.HEAD : SVNRevision.create( revision );

            return updateClient.doCheckout( url, localPath, SVNRevision.HEAD, rev, SVNDepth.INFINITY, true );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public long commit( File[] wcPaths, String comment )
        throws SCMException
    {
        try
        {
            File wcRoot = SVNWCUtil.getWorkingCopyRoot( wcPaths[0], false );
            cleanup( wcRoot );

            SVNCommitClient commitClient = clientManager.getCommitClient();
            SVNCommitPacket packet = commitClient.doCollectCommitItems( wcPaths, false, true, SVNDepth.EMPTY, null );

            SVNCommitInfo commitInfo = commitClient.doCommit( packet, false, comment );
            SVNErrorMessage error = commitInfo.getErrorMessage();
            if ( error != null )
            {
                LOG.debug( error.getFullMessage() );
                throw new SCMException( error.getFullMessage() );
            }
            return commitInfo.getNewRevision();
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void cleanup( File wcPath )
        throws SCMException
    {
        try
        {
            clientManager.getWCClient().doCleanup( wcPath );
        }
        catch ( SVNException e )
        {
            lastInfo = e.getMessage();
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void delete( File wcPath )
        throws SCMException
    {
        try
        {
            delete( wcPath, true );
        }
        catch ( SVNException e )
        {
            lastInfo = e.getMessage();
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public Map<String, ?> info( File wcPath )
        throws SCMException
    {
        try
        {
            Map properties = new HashMap();

            final SVNInfo[] result = new SVNInfo[1];
            clientManager.getWCClient().doInfo( wcPath, SVNRevision.UNDEFINED, SVNRevision.WORKING, SVNDepth.EMPTY,
                null, new ISVNInfoHandler()
                {
                    @Override
                    public void handleInfo( SVNInfo info )
                    {
                        if ( result[0] == null )
                        {
                            result[0] = info;
                        }
                    }
                } );
            SVNInfo info = result[0];

            properties.put( "author", info.getAuthor() );
            properties.put( "committed-date", info.getCommittedDate() );
            properties.put( "committed-rev", Long.valueOf( info.getCommittedRevision().getNumber() ) );
            properties.put( "revision", Long.valueOf( info.getRevision().getNumber() ) );

            SVNLock lock = info.getLock();
            if ( lock != null )
            {
                properties.put( "lock", lock.getID() );
                properties.put( "lock.owner", lock.getOwner() );
                properties.put( "lock.comment", lock.getComment() );
                properties.put( "lock.creationDate", lock.getCreationDate() );
                properties.put( "lock.expirationDate", lock.getExpirationDate() );
            }
            return properties;
        }
        catch ( SVNException e )
        {
        }
        return null;
    }

    @Override
    public void move( File src, File dst )
        throws SCMException
    {
        try
        {
            clientManager.getMoveClient().doMove( src, dst );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void lock( File wcPath, String comment )
        throws SCMException
    {
        try
        {
            lock( wcPath, true, comment );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void unlock( File wcPath, boolean breakLock )
        throws SCMException
    {
        try
        {
            clientManager.getWCClient().doUnlock( new File[] { wcPath }, breakLock );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public Map<String, ?> status( File wcPath )
        throws SCMException
    {
        try
        {
            Map properties = new HashMap();
            SVNStatus status = clientManager.getStatusClient().doStatus( wcPath, false, true );

            properties.put( "url", status.getURL() );
            properties.put( "author", status.getAuthor() );
            properties.put( "committed-date", status.getCommittedDate() );
            properties.put( "committed-rev", Long.valueOf( status.getCommittedRevision().getNumber() ) );
            properties.put( "revision", Long.valueOf( status.getRevision().getNumber() ) );
            properties.put( "status", Character.valueOf( status.getNodeStatus().getCode() ) );

            return properties;
        }
        catch ( SVNException e )
        {
        }
        return null;
    }

    @Override
    public long synchronize( File wcPath )
        throws SCMException
    {
        long revision = update( wcPath, 0L );

        LOG.debug( "Update complete, revision: " + revision );
        try
        {
            Collection globalIgnores = SVNStatusEditor.getGlobalIgnores( clientManager.getOptions() );

            File[] files = wcPath.listFiles();
            for ( File file : files )
            {
                if ( SVNStatusEditor.isIgnored( globalIgnores, file ) )
                {
                    continue;
                }

                if ( !( SVNWCUtil.isVersionedDirectory( file ) ) )
                {
                    LOG.debug( "Add: " + file );
                    add( new File[] { file }, true );
                }
            }
        }
        catch ( Exception e )
        {
            throw new SCMException( e.getMessage(), e );
        }

        LOG.debug( "Commit: " + wcPath );

        long rev = commit( new File[] { wcPath }, "synchronize" );
        return ( ( rev == -1L ) ? revision : rev );
    }

    @Override
    public long update( File wcPath, long revision )
        throws SCMException
    {
        try
        {
            SVNUpdateClient updateClient = clientManager.getUpdateClient();

            updateClient.setIgnoreExternals( false );

            SVNRevision rev = ( revision > 0L ) ? SVNRevision.create( revision ) : SVNRevision.HEAD;
            long result = updateClient.doUpdate( wcPath, rev, SVNDepth.INFINITY, true, true );

            return result;
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void relocate( File wcRoot, String path )
        throws SCMException
    {
        try
        {
            SVNStatus status = clientManager.getStatusClient().doStatus( wcRoot, true );
            SVNURL oldURL = status.getURL();
            SVNURL newURL = repositoryURL.appendPath( path, false );
            SVNUpdateClient updateClient = clientManager.getUpdateClient();
            updateClient.doRelocate( wcRoot, oldURL, newURL, true );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void resolve( File wcPath, String choice )
        throws SCMException
    {
        try
        {
            SVNConflictChoice conflictChoice = SVNConflictChoice.POSTPONE;
            if ( choice.equals( "base" ) )
            {
                conflictChoice = SVNConflictChoice.BASE;
            }
            else if ( choice.equals( "merged" ) )
            {
                conflictChoice = SVNConflictChoice.MERGED;
            }
            else if ( choice.equals( "mine" ) )
            {
                conflictChoice = SVNConflictChoice.MINE_FULL;
            }
            else if ( choice.equals( "incoming" ) )
            {
                conflictChoice = SVNConflictChoice.THEIRS_FULL;
            }
            clientManager.getWCClient().doResolve( wcPath, SVNDepth.INFINITY, conflictChoice );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    @Override
    public void revert( File wcPath )
        throws SCMException
    {
        try
        {
            clientManager.getWCClient().doRevert( new File[] { wcPath }, SVNDepth.INFINITY, null );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    public String getLastStatus()
    {
        return lastInfo;
    }

    @Override
    public void dispose()
    {
        clientManager.dispose();
    }

    @Override
    public boolean isIgnored( File path )
    {
        Collection globalIgnores = SVNStatusEditor.getGlobalIgnores( clientManager.getOptions() );
        return SVNStatusEditor.isIgnored( globalIgnores, path );
    }

    @Override
    public boolean isVersioned( File location )
    {
        return SCM.isVersioned( location );
    }

    public void init()
    {
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions( true );
        options.addIgnorePattern( ".svn" );
        options.addIgnorePattern( ".cvs" );
        options.addIgnorePattern( "bin" );
        options.addIgnorePattern( "*.mine" );
        options.addIgnorePattern( "target" );
        options.addIgnorePattern( "classes" );
        options.setAuthStorageEnabled( false );

        authManager = new SVNAuthenticationManager( name, password );

        clientManager = SVNClientManager.newInstance();

        clientManager.setOptions( options );
        clientManager.setAuthenticationManager( authManager );
    }

    public SVNCommitInfo makeDirectory( String dir, String commitMessage )
        throws SVNException
    {
        SVNURL url = repositoryURL.appendPath( dir, false );

        return clientManager.getCommitClient().doMkDir( new SVNURL[] { url }, commitMessage, null, true );
    }

    public SVNCommitInfo deleteDirectory( String dir, String commitMessage )
        throws SVNException
    {
        SVNURL url = repositoryURL.appendPath( dir, false );

        return clientManager.getCommitClient().doDelete( new SVNURL[] { url }, commitMessage );
    }

    public long switchToURL( File wcPath, String dir, SVNRevision updateToRevision, boolean isRecursive )
        throws SVNException
    {
        SVNURL url = repositoryURL.appendPath( dir, false );
        SVNUpdateClient updateClient = clientManager.getUpdateClient();

        updateClient.setIgnoreExternals( false );

        return updateClient.doSwitch( wcPath, url, SVNRevision.UNDEFINED, updateToRevision,
            SVNDepth.getInfinityOrFilesDepth( isRecursive ), false, false );
    }

    private void lock( File wcPath, boolean isStealLock, String lockComment )
        throws SVNException
    {
        clientManager.getWCClient().doLock( new File[] { wcPath }, isStealLock, lockComment );
    }

    public void delete( File wcPath, boolean force )
        throws SVNException
    {
        clientManager.getWCClient().doDelete( wcPath, force, false );
    }

    public SVNCommitInfo copy( String src, String dst, boolean isMove, String commitMessage )
        throws SVNException
    {
        SVNURL srcURL = repositoryURL.appendPath( src, false );
        SVNURL dstURL = repositoryURL.appendPath( dst, false );

        SVNCopySource[] sources = new SVNCopySource[1];
        sources[0] = new SVNCopySource( SVNRevision.UNDEFINED, SVNRevision.HEAD, srcURL );

        return clientManager.getCopyClient().doCopy( sources, dstURL, isMove, true, true, commitMessage, null );
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }

    public void setRepositoryURL( String repositoryURL )
    {
        url = repositoryURL;
        try
        {
            this.repositoryURL = SVNURL.parseURIEncoded( repositoryURL );
        }
        catch ( SVNException e )
        {
            throw new RuntimeException( e.getMessage() );
        }
    }

    public String getRepositoryURL()
    {
        return url;
    }

    public SVNURL getSVNURL()
    {
        return repositoryURL;
    }
}