package com.sanxing.sesame.mbean;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.LifeCycleMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.deployment.Component;
import com.sanxing.sesame.deployment.Descriptor;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.platform.events.ArchiveEvent;
import com.sanxing.sesame.util.FileUtil;
import com.sanxing.sesame.util.XmlPersistenceSupport;

public class ArchiveManager
    extends BaseSystemService
{
    private static Logger LOG = LoggerFactory.getLogger( ArchiveManager.class );

    private boolean monitorInstallationDirectory;

    private boolean monitorDeploymentDirectory;

    private static String filePrefix = "file:///";

    private InstallationService installationService;

    private DeploymentService deploymentService;

    private EnvironmentContext environmentContext;

    private final Map<File, ArchiveEntry> pendingComponents;

    private final Map<File, ArchiveEntry> pendingSAs;

    private Map<String, ArchiveEntry> installFileMap;

    private Map<String, ArchiveEntry> deployFileMap;

    private final Set<ArchiveEvent> publishedEvents;

    private final String extensions;

    public ArchiveManager()
    {
        monitorInstallationDirectory = true;

        monitorDeploymentDirectory = true;

        pendingComponents = new ConcurrentHashMap();

        pendingSAs = new ConcurrentHashMap();

        publishedEvents = new HashSet();

        extensions = ".zip,.jar";
    }

    public Set<ArchiveEvent> getPublishedEvents()
    {
        return publishedEvents;
    }

    public Map<String, ArchiveEntry> getInstallFileMap()
    {
        return installFileMap;
    }

    public void setInstallFileMap( Map<String, ArchiveEntry> installFileMap )
    {
        this.installFileMap = installFileMap;
    }

    public Map<String, ArchiveEntry> getDeployFileMap()
    {
        return deployFileMap;
    }

    public void setDeployFileMap( Map<String, ArchiveEntry> deployFileMap )
    {
        this.deployFileMap = deployFileMap;
    }

    @Override
    public void init( JBIContainer cont )
        throws JBIException
    {
        super.init( cont );
        deploymentService = container.getDeploymentService();
        installationService = container.getInstallationService();
        environmentContext = container.getEnvironmentContext();
        initializeFileMaps();
    }

    public boolean isMonitorInstallationDirectory()
    {
        return monitorInstallationDirectory;
    }

    public void setMonitorInstallationDirectory( boolean monitorInstallationDirectory )
    {
        this.monitorInstallationDirectory = monitorInstallationDirectory;
    }

    public boolean isMonitorDeploymentDirectory()
    {
        return monitorDeploymentDirectory;
    }

    public void setMonitorDeploymentDirectory( boolean monitorDeploymentDirectory )
    {
        this.monitorDeploymentDirectory = monitorDeploymentDirectory;
    }

    public void updateArchive( String location, ArchiveEntry entry, boolean autoStart )
        throws DeploymentException
    {
        File tmpDir = null;
        try
        {
            tmpDir = unpackLocation( environmentContext.getTmpDir(), location );
        }
        catch ( Exception e )
        {
            throw failure( "deploy", "Unable to unpack archive: " + location, e );
        }

        if ( tmpDir == null )
        {
            throw failure( "deploy", "Unable to find jbi descriptor: " + location );
        }
        Descriptor root = null;
        try
        {
            root = DescriptorFactory.buildDescriptor( tmpDir );
        }
        catch ( Exception e )
        {
            throw failure( "deploy", "Unable to build jbi descriptor: " + location, e );
        }

        if ( root == null )
        {
            throw failure( "deploy", "Unable to find jbi descriptor: " + location );
        }

        if ( ( Platform.getEnv().isAdmin() ) && ( Platform.getEnv().isProduction() ) )
        {
            ClusterAdminMBean admin = MBeanHelper.getAdminMBean( ClusterAdminMBean.class, "cluster-manager" );
            ArchiveEntry eventEntry = new ArchiveEntry();
            eventEntry.setLocation( location );
            eventEntry.setName( entry.getName() );
            eventEntry.setLastModified( entry.getLastModified() );
            ArchiveEvent ae = new ArchiveEvent( eventEntry );
            admin.notifyNeighbors( ae );

            if ( root.getComponent() != null )
            {
                persistState( new File( location ).getParentFile(), installFileMap );
            }
            else if ( root.getSharedLibrary() != null )
            {
                persistState( new File( location ).getParentFile(), installFileMap );
            }
            else if ( root.getServiceAssembly() != null )
            {
                persistState( new File( location ).getParentFile(), deployFileMap );
            }
            tmpDir.delete();

            publishedEvents.add( ae );
        }
        else
        {
            try
            {
                container.getRouter().suspend();
                if ( root.getComponent() != null )
                {
                    updateComponent( entry, autoStart, tmpDir, root );
                    persistState( new File( location ).getParentFile(), installFileMap );
                }
                else if ( root.getSharedLibrary() != null )
                {
                    updateSharedLibrary( entry, tmpDir, root );
                    persistState( new File( location ).getParentFile(), installFileMap );
                }
                else if ( root.getServiceAssembly() != null )
                {
                    updateServiceAssembly( entry, autoStart, tmpDir, root );
                    persistState( new File( location ).getParentFile(), deployFileMap );
                }
            }
            finally
            {
                container.getRouter().resume();
            }
        }
    }

    protected DeploymentException failure( String task, String info )
    {
        return failure( task, info, null, null );
    }

    protected DeploymentException failure( String task, String info, Exception e )
    {
        return failure( task, info, e, null );
    }

    protected DeploymentException failure( String task, String info, Exception e, List componentResults )
    {
        ManagementSupport.Message msg = new ManagementSupport.Message();
        msg.setTask( task );
        msg.setResult( "FAILED" );
        msg.setType( "ERROR" );
        msg.setException( e );
        msg.setMessage( info );
        return new DeploymentException( ManagementSupport.createFrameworkMessage( msg, componentResults ) );
    }

    protected void updateSharedLibrary( ArchiveEntry entry, File tmpDir, Descriptor root )
        throws DeploymentException
    {
        String libraryName = root.getSharedLibrary().getIdentification().getName();
        entry.type = "library";
        entry.name = libraryName;
        try
        {
            if ( container.getRegistry().getSharedLibrary( libraryName ) != null )
            {
                container.getRegistry().unregisterSharedLibrary( libraryName );
                environmentContext.removeSharedLibraryDirectory( libraryName );
            }
            installationService.doInstallSharedLibrary( tmpDir, root.getSharedLibrary() );
            checkPendingComponents();
        }
        catch ( Exception e )
        {
            String errStr = "Failed to update SharedLibrary: " + libraryName;
            LOG.error( errStr, e );
            throw new DeploymentException( errStr, e );
        }
    }

    protected Set<String> getComponentNames( ServiceAssembly sa )
    {
        Set names = new HashSet();
        if ( ( sa.getServiceUnits() != null ) && ( sa.getServiceUnits().length > 0 ) )
        {
            for ( int i = 0; i < sa.getServiceUnits().length; ++i )
            {
                names.add( sa.getServiceUnits()[i].getTarget().getComponentName() );
            }
        }
        return names;
    }

    private void checkPendingComponents()
    {
        Set<File> installedComponents = new HashSet();
        for ( Map.Entry me : pendingComponents.entrySet() )
        {
            ArchiveEntry entry = (ArchiveEntry) me.getValue();
            boolean canInstall = true;
            for ( String libraryName : entry.dependencies )
            {
                if ( container.getRegistry().getSharedLibrary( libraryName ) == null )
                {
                    canInstall = false;
                    break;
                }
            }
            if ( canInstall )
            {
                File tmp = (File) me.getKey();
                installedComponents.add( tmp );
                try
                {
                    Descriptor root = DescriptorFactory.buildDescriptor( tmp );
                    installationService.install( tmp, null, root, true );
                }
                catch ( Exception e )
                {
                    String errStr = "Failed to update Component: " + tmp.getName();
                    LOG.error( errStr, e );
                }
            }
        }
        if ( installedComponents.isEmpty() )
        {
            return;
        }
        for ( File f : installedComponents )
        {
            ArchiveEntry entry = pendingComponents.remove( f );
            entry.pending = false;
        }

        persistState( environmentContext.getDeploymentDir(), deployFileMap );
        persistState( environmentContext.getInstallationDir(), installFileMap );

        checkPendingSAs();
    }

    protected void updateServiceAssembly( ArchiveEntry entry, boolean autoStart, File tmpDir, Descriptor root )
        throws DeploymentException
    {
        ServiceAssembly sa = root.getServiceAssembly();
        String name = sa.getIdentification().getName();
        entry.type = "assembly";
        entry.name = name;
        try
        {
            if ( deploymentService.isSaDeployed( name ) )
            {
                deploymentService.shutDown( name );
                deploymentService.undeploy( name );
            }

            entry.dependencies = getComponentNames( sa );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "SA dependencies: " + entry.dependencies );
            }
            String missings = null;
            boolean canDeploy = true;
            for ( String componentName : entry.dependencies )
            {
                if ( container.getRegistry().getComponent( componentName ) == null )
                {
                    canDeploy = false;
                    if ( missings != null )
                    {
                        missings = missings + ", " + componentName;
                    }
                    else
                    {
                        missings = componentName;
                    }
                }
            }
            if ( canDeploy )
            {
                deploymentService.deployServiceAssembly( tmpDir, sa );
                if ( autoStart )
                {
                    deploymentService.start( name );
                }
            }
            else
            {
                entry.pending = true;
                LOG.warn( "Components " + missings + " are not installed yet: the service assembly " + name
                    + " deployment is suspended and will be resumed once the listed components are installed" );

                pendingSAs.put( tmpDir, entry );

                throw ManagementSupport.failure( "deploy", missings + "组件未安装！" );
            }
        }
        catch ( Exception e )
        {
            String errStr = "Failed to update Service Assembly: " + name;
            LOG.error( errStr, e );
            throw new DeploymentException( errStr, e );
        }
    }

    protected void updateComponent( ArchiveEntry entry, boolean autoStart, File tmpDir, Descriptor root )
        throws DeploymentException
    {
        Component comp = root.getComponent();
        String componentName = comp.getIdentification().getName();
        entry.type = "component";
        entry.name = componentName;
        try
        {
            if ( container.getRegistry().getComponent( componentName ) != null )
            {
                installationService.loadInstaller( componentName );
                installationService.unloadInstaller( componentName, true );
            }

            entry.dependencies = getSharedLibraryNames( comp );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Component dependencies: " + entry.dependencies );
            }
            String missings = null;
            boolean canInstall = true;
            for ( String libraryName : entry.dependencies )
            {
                if ( container.getRegistry().getSharedLibrary( libraryName ) == null )
                {
                    canInstall = false;
                    if ( missings != null )
                    {
                        missings = missings + ", " + libraryName;
                    }
                    else
                    {
                        missings = libraryName;
                    }
                }
            }
            if ( canInstall )
            {
                installationService.install( tmpDir, null, root, autoStart );
                checkPendingSAs();
            }
            else
            {
                entry.pending = true;
                LOG.warn( "Shared libraries " + missings + " are not installed yet: the component" + componentName
                    + " installation is suspended and will be resumed once the listed shared libraries are installed" );

                pendingComponents.put( tmpDir, entry );
            }
        }
        catch ( DeploymentException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new DeploymentException( "Failed to update Component: " + componentName, e );
        }
    }

    private void checkPendingSAs()
    {
        Set<File> deployedSas = new HashSet();
        for ( Map.Entry me : pendingSAs.entrySet() )
        {
            ArchiveEntry entry = (ArchiveEntry) me.getValue();
            boolean canDeploy = true;
            for ( String componentName : entry.dependencies )
            {
                if ( container.getRegistry().getComponent( componentName ) == null )
                {
                    canDeploy = false;
                    break;
                }
            }
            if ( canDeploy )
            {
                File tmp = (File) me.getKey();
                deployedSas.add( tmp );
                try
                {
                    Descriptor root = DescriptorFactory.buildDescriptor( tmp );
                    deploymentService.deployServiceAssembly( tmp, root.getServiceAssembly() );
                    deploymentService.start( root.getServiceAssembly().getIdentification().getName() );
                }
                catch ( Exception e )
                {
                    String errStr = "Failed to update Service Assembly: " + tmp.getName();
                    LOG.error( errStr, e );
                }
            }
        }
        if ( deployedSas.isEmpty() )
        {
            return;
        }
        for ( File f : deployedSas )
        {
            ArchiveEntry entry = pendingSAs.remove( f );
            entry.pending = false;
        }

        persistState( environmentContext.getDeploymentDir(), deployFileMap );
        persistState( environmentContext.getInstallationDir(), installFileMap );
    }

    public void scanInstallDir()
    {
        scanDir( environmentContext.getInstallationDir(), installFileMap );
    }

    public void scanDeployDir()
    {
        scanDir( environmentContext.getDeploymentDir(), deployFileMap );
    }

    private void scanDir( File root, Map<String, ArchiveEntry> fileMap )
    {
        List tmpList = new ArrayList();
        if ( ( root != null ) && ( root.exists() ) && ( root.isDirectory() ) )
        {
            File[] files = root.listFiles();
            if ( files != null )
            {
                for ( int i = 0; i < files.length; ++i )
                {
                    File file = files[i];
                    tmpList.add( file.getName() );
                    if ( ( isAllowedExtension( file.getName() ) ) && ( isAvailable( file ) ) )
                    {
                        ArchiveEntry lastEntry = fileMap.get( file.getName() );
                        if ( ( lastEntry != null ) && ( file.lastModified() <= lastEntry.lastModified.getTime() ) )
                        {
                            continue;
                        }
                        try
                        {
                            ArchiveEntry entry = new ArchiveEntry();
                            entry.location = file.getName();
                            entry.lastModified = new Date( file.lastModified() );
                            fileMap.put( file.getName(), entry );
                            LOG.info( "Directory: " + root.getName() + ": Archive changed: processing "
                                + file.getName() + " ..." );
                            updateArchive( file.getAbsolutePath(), entry, true );
                            LOG.info( "Directory: " + root.getName() + ": Finished installation of archive:  "
                                + file.getName() );
                        }
                        catch ( Exception e )
                        {
                            LOG.warn( "Directory: " + root.getName() + ": Automatic install of " + file + " failed", e );
                        }
                    }
                }

            }

            Map map = new HashMap( fileMap );
            for ( Object location : map.keySet() )
            {
                if ( !( tmpList.contains( location ) ) )
                {
                    ArchiveEntry entry = fileMap.remove( location );
                    try
                    {
                        LOG.info( "Location " + location + " no longer exists - removing ..." );
                        removeArchive( entry );
                    }
                    catch ( DeploymentException e )
                    {
                        LOG.error( "Failed to removeArchive: " + location, e );
                    }
                }
            }
            if ( !( map.equals( fileMap ) ) )
            {
                persistState( root, fileMap );
            }
        }
    }

    public void persistState( File root, Map<String, ArchiveEntry> map )
    {
        try
        {
            File file = new File( environmentContext.getJbiRootDir(), root.getName() + ".xml" );
            XmlPersistenceSupport.write( file, map );
        }
        catch ( IOException e )
        {
            LOG.error( "Failed to persist file state to: " + root, e );
        }
    }

    private Map<String, ArchiveEntry> readState( File root )
    {
        Map result = new HashMap();
        try
        {
            File file = new File( environmentContext.getJbiRootDir(), root.getName() + ".xml" );
            if ( file.exists() )
            {
                result = (Map) XmlPersistenceSupport.read( file );
            }
            else
            {
                LOG.debug( "State file doesn't exist: " + file.getPath() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to read file state from: " + root, e );
        }
        return result;
    }

    private void initializeFileMaps()
    {
        if ( isMonitorInstallationDirectory() )
        {
            try
            {
                installFileMap = readState( environmentContext.getInstallationDir() );
                removePendingEntries( installFileMap );
            }
            catch ( Exception e )
            {
                LOG.error( "Failed to read installed state", e );
            }
        }
        if ( !( isMonitorDeploymentDirectory() ) )
        {
            return;
        }
        try
        {
            deployFileMap = readState( environmentContext.getDeploymentDir() );
            removePendingEntries( deployFileMap );
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to read deployed state", e );
        }
    }

    private void removePendingEntries( Map<String, ArchiveEntry> map )
    {
        Set pendings = new HashSet();
        for ( Map.Entry e : map.entrySet() )
        {
            if ( ( (ArchiveEntry) e.getValue() ).pending )
            {
                pendings.add( e.getKey() );
            }
        }
        for ( Object s : pendings )
        {
            map.remove( s );
        }
    }

    protected Set<String> getSharedLibraryNames( Component comp )
    {
        Set names = new HashSet();
        if ( ( comp.getSharedLibraries() != null ) && ( comp.getSharedLibraries().length > 0 ) )
        {
            for ( int i = 0; i < comp.getSharedLibraries().length; ++i )
            {
                names.add( comp.getSharedLibraries()[i].getName() );
            }
        }
        return names;
    }

    public static File unpackLocation( File tmpRoot, String location )
        throws DeploymentException
    {
        File tmpDir = null;
        File file = null;
        try
        {
            if ( location.startsWith( filePrefix ) )
            {
                String os = System.getProperty( "os.name" );
                if ( os.startsWith( "Windows" ) )
                {
                    location = location.replace( '\\', '/' );
                    location = location.replaceAll( " ", "%20" );
                }
                URI uri = new URI( location );
                file = new File( uri );
            }
            else
            {
                file = new File( location );
            }
            if ( file.isDirectory() )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Deploying an exploded jar/zip, we will create a temporary jar for it." );
                }

                File newFile = new File( tmpRoot.getAbsolutePath() + "/exploded.jar" );
                newFile.delete();
                FileUtil.zipDir( file.getAbsolutePath(), newFile.getAbsolutePath() );
                file = newFile;
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Deployment will now work from " + file.getAbsolutePath() );
                }
            }
            if ( !( file.exists() ) )
            {
                try
                {
                    URL url = new URL( location );
                    String fileName = url.getFile();
                    if ( fileName == null )
                    {
                        throw new DeploymentException( "Location: " + location + " is not an archive" );
                    }
                    file = FileUtil.unpackArchive( url, tmpRoot );
                }
                catch ( MalformedURLException e )
                {
                    throw new DeploymentException( e );
                }
            }
            if ( FileUtil.archiveContainsEntry( file, "jbi.xml" ) )
            {
                tmpDir = FileUtil.createUniqueDirectory( tmpRoot, file.getName() );
                FileUtil.unpackArchive( file, tmpDir );
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Unpacked archive " + location + " to " + tmpDir );
                }
            }
        }
        catch ( IOException e )
        {
            throw new DeploymentException( e );
        }
        catch ( URISyntaxException ex )
        {
            throw new DeploymentException( ex );
        }
        return tmpDir;
    }

    @Override
    protected Class getServiceMBean()
    {
        return LifeCycleMBean.class;
    }

    @Override
    public String getDescription()
    {
        return "archiva manager";
    }

    public void removeArchive( ArchiveEntry entry )
        throws DeploymentException
    {
        LOG.info( "Attempting to remove archive at: " + entry.location );
        try
        {
            container.getRouter().suspend();
            if ( "component".equals( entry.type ) )
            {
                LOG.info( "Uninstalling component: " + entry.name );

                installationService.loadInstaller( entry.name );

                installationService.unloadInstaller( entry.name, true );
            }
            if ( "library".equals( entry.type ) )
            {
                LOG.info( "Removing shared library: " + entry.name );
                installationService.uninstallSharedLibrary( entry.name );
            }
            if ( "assembly".equals( entry.type ) )
            {
                LOG.info( "Undeploying service assembly " + entry.name );
                try
                {
                    if ( deploymentService.isSaDeployed( entry.name ) )
                    {
                        deploymentService.shutDown( entry.name );
                        deploymentService.undeploy( entry.name );
                    }
                }
                catch ( Exception e )
                {
                    String errStr = "Failed to update service assembly: " + entry.name;
                    LOG.error( errStr, e );
                    throw new DeploymentException( errStr, e );
                }
            }
        }
        finally
        {
            container.getRouter().resume();
        }
    }

    public String getExtensions()
    {
        return extensions;
    }

    public boolean isAllowedExtension( String file )
    {
        String[] ext = extensions.split( "," );
        for ( int i = 0; i < ext.length; ++i )
        {
            if ( file.endsWith( ext[i] ) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean isAvailable( File file )
    {
        long targetLength = file.length();
        try
        {
            Thread.sleep( 100L );
        }
        catch ( InterruptedException e )
        {
        }
        long target2Length = file.length();

        if ( targetLength != target2Length )
        {
            LOG.warn( "File is still being copied, deployment deferred to next cycle: " + file.getName() );
            return false;
        }

        try
        {
            ZipFile zip = new ZipFile( file );
            zip.size();
            zip.close();
        }
        catch ( IOException e )
        {
            LOG.warn( "Unable to open deployment file, deployment deferred to next cycle: " + file.getName() );
            return false;
        }
        return true;
    }

    public static void main( String[] args )
    {
    }
}