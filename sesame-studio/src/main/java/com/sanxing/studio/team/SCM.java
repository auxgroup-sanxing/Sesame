package com.sanxing.studio.team;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminArea;
import org.tmatesoft.svn.core.internal.wc.admin.SVNEntry;
import org.tmatesoft.svn.core.internal.wc.admin.SVNWCAccess;
import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb;
import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.SVNWCDbOpenMode;
import org.tmatesoft.svn.core.internal.wc17.db.SVNWCDb;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.sanxing.sesame.pwd.PasswordTool;
import com.sanxing.studio.Application;
import com.sanxing.studio.Configuration;
import com.sanxing.studio.team.svn.SVNAuthenticationManager;
import com.sanxing.studio.team.svn.SVNSynchronizer;

public class SCM
{
    private static final Logger LOG = LoggerFactory.getLogger( SCM.class );

    private static Map<String, SVNRepository> reposCache = new Hashtable();

    private static Map<File, ThreeWaySynchronizer> syncCache = new Hashtable();

    private static ISVNAuthenticationManager authManager = new SVNAuthenticationManager( "root", "" );

    public static synchronized ThreeWaySynchronizer getSynchronizer( File location, String repositoryURL,
                                                                     String username, String password )
    {
        ThreeWaySynchronizer synchronizer = syncCache.get( location );
        if ( synchronizer == null )
        {
            synchronizer = new SVNSynchronizer( repositoryURL, username, password );
            syncCache.put( location, synchronizer );
        }
        return synchronizer;
    }

    public static synchronized ThreeWaySynchronizer getSynchronizer( File location )
    {
        ThreeWaySynchronizer synchronizer = syncCache.get( location );
        if ( synchronizer == null )
        {
            String repositoryURL = null;
            SVNWCDb db = new SVNWCDb();
            try
            {
                db.open( SVNWCDbOpenMode.ReadOnly, null, false, false );
                ISVNWCDb.WCDbInfo info =
                    db.readInfo( location.getAbsoluteFile(),
                        new ISVNWCDb.WCDbInfo.InfoField[] { ISVNWCDb.WCDbInfo.InfoField.reposRootUrl } );
                if ( info != null )
                {
                    repositoryURL = info.reposRootUrl.removePathTail().toString();
                }
            }
            catch ( SVNException e )
            {
            }
            finally
            {
                db.close();
            }

            if ( repositoryURL == null )
            {
                SVNWCAccess wcAccess = SVNWCAccess.newInstance( null );
                try
                {
                    wcAccess.open( location, false, 0 );
                    SVNEntry entry = wcAccess.getEntry( location, false );
                    repositoryURL = entry.getSVNURL().removePathTail().toString();
                }
                catch ( SVNException e )
                {
                }
                finally
                {
                    try
                    {
                        wcAccess.close();
                    }
                    catch ( SVNException e )
                    {
                    }
                }
            }
            if ( repositoryURL == null )
            {
                return null;
            }
            String username = null;
            String password = null;
            Element prefsEl;
            try
            {
                prefsEl = Configuration.getSCMPrefs();
                Element repoEl = (Element) XPath.selectSingleNode( prefsEl, "repository[@url='" + repositoryURL + "']" );
                if ( repoEl != null )
                {
                    username = repoEl.getAttributeValue( "username" );
                    password = PasswordTool.decrypt( repoEl.getAttributeValue( "password" ) );
                }
            }
            catch ( Exception e )
            {
            }
            synchronizer = new SVNSynchronizer( repositoryURL, username, password );
            syncCache.put( location, synchronizer );
        }
        return synchronizer;
    }

    public static synchronized ThreeWaySynchronizer getSynchronizer( String path )
    {
        File location = Application.getWorkspaceFile( path );
        return getSynchronizer( location );
    }

    public static synchronized void removeSynchronizer( File path )
    {
        syncCache.remove( path );
    }

    public static synchronized void cleanUp()
    {
        Collection<ThreeWaySynchronizer> collection = syncCache.values();
        for ( ThreeWaySynchronizer sync : collection )
        {
            sync.dispose();
        }
    }

    public static boolean isVersioned( File location )
    {
        if ( location.isDirectory() )
        {
            return SVNWCUtil.isVersionedDirectory( location );
        }

        SVNWCDb db = new SVNWCDb();
        try
        {
            db.open( SVNWCDbOpenMode.ReadOnly, null, false, false );
            ISVNWCDb.WCDbInfo info =
                db.readInfo( location.getAbsoluteFile(),
                    new ISVNWCDb.WCDbInfo.InfoField[] { ISVNWCDb.WCDbInfo.InfoField.revision } );
            if ( info != null )
            {
                return true;
            }
        }
        catch ( SVNException e )
        {
        }
        finally
        {
            db.close();
        }

        SVNWCAccess wcAccess = SVNWCAccess.newInstance( null );
        try
        {
            SVNAdminArea area = wcAccess.open( location.getParentFile(), false, false, false, 0, Level.FINEST );
            return area.getEntry( location.getName(), true ) != null;
        }
        catch ( SVNException e )
        {
            return false;
        }
        finally
        {
            try
            {
                wcAccess.close();
            }
            catch ( SVNException e )
            {
            }
        }
    }

    public static Map<String, Object> getVersionInfo( File location )
    {
        SVNWCDb db = new SVNWCDb();
        try
        {
            db.open( SVNWCDbOpenMode.ReadOnly, null, false, false );
            ISVNWCDb.WCDbInfo info =
                db.readInfo( location.getAbsoluteFile(), new ISVNWCDb.WCDbInfo.InfoField[] {
                    ISVNWCDb.WCDbInfo.InfoField.reposRelPath, ISVNWCDb.WCDbInfo.InfoField.reposRootUrl,
                    ISVNWCDb.WCDbInfo.InfoField.changedAuthor, ISVNWCDb.WCDbInfo.InfoField.changedRev,
                    ISVNWCDb.WCDbInfo.InfoField.lock, ISVNWCDb.WCDbInfo.InfoField.revision } );
            if ( info != null )
            {
                Map result = new HashMap();
                result.put( "url", info.reposRootUrl.toString() + "/"
                    + info.reposRelPath.toString().replace( "\\", "/" ) );
                result.put( "author", info.changedAuthor );
                result.put( "committed-rev", Long.valueOf( info.changedRev ) );
                result.put( "lockOwner", info.lock == null ? "" : info.lock.owner );
                result.put( "revision", Long.valueOf( info.revision ) );

                return result;
            }
        }
        catch ( SVNException e )
        {
        }
        finally
        {
            db.close();
        }

        SVNWCAccess wcAccess = SVNWCAccess.newInstance( null );
        String url;
        try
        {
            wcAccess.probeOpen( location, false, 0 );
            SVNEntry entry = wcAccess.getEntry( location, false );
            if ( entry == null )
            {
                return null;
            }
            url = entry.getURL();
            Map result = new HashMap();
            result.put( "url", url );
            result.put( "author", entry.getAuthor() );
            result.put( "committed-rev", Long.valueOf( entry.getCommittedRevision() ) );
            result.put( "lockOwner", entry.getLockOwner() );
            result.put( "revision", Long.valueOf( entry.getRevision() ) );

            return result;
        }
        catch ( SVNException e )
        {
            return null;
        }
        finally
        {
            try
            {
                wcAccess.close();
            }
            catch ( SVNException e )
            {
            }
        }
    }

    private static SVNRepository getSVNRepository( String repositoryURL )
        throws SVNException
    {
        SVNRepository repository = reposCache.get( repositoryURL );
        if ( repository == null )
        {
            repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( repositoryURL ) );
            repository.setAuthenticationManager( authManager );
            reposCache.put( repositoryURL, repository );
        }
        return repository;
    }

    public static PathEntry[] getEntries( String repositoryURL, String path )
        throws SCMException
    {
        List list = new ArrayList();
        try
        {
            SVNRepository repository = getSVNRepository( repositoryURL );
            Collection c = null;
            Collection<SVNDirEntry> entries = repository.getDir( path, SVNRevision.HEAD.getNumber(), null, c );
            for ( SVNDirEntry entry : entries )
            {
                PathEntry e = new PathEntry();
                e.setAuthor( entry.getAuthor() );
                e.setDate( entry.getDate() );
                e.setName( entry.getName() );
                e.setRelativePath( entry.getRelativePath() );
                e.setRevision( entry.getRevision() );
                e.setSize( entry.getSize() );
                e.setKind( ( entry.getKind() == SVNNodeKind.FILE ) ? "file"
                    : ( entry.getKind() == SVNNodeKind.DIR ) ? "dir" : "none" );

                list.add( e );
            }

            return ( (PathEntry[]) list.toArray( new PathEntry[list.size()] ) );
        }
        catch ( SVNException e )
        {
            throw new SCMException( e.getMessage(), e );
        }
    }

    static
    {
        SVNRepositoryFactoryImpl.setup();

        FSRepositoryFactory.setup();

        DAVRepositoryFactory.setup();
    }
}