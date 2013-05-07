package com.sanxing.sesame.container;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.mbean.BaseSystemService;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.util.FileUtil;
import com.sanxing.sesame.util.FileVersionUtil;

public class EnvironmentContext
    extends BaseSystemService
    implements EnvironmentContextMBean
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentContext.class );

    private File jbiRootDir;

    private File componentsDir;

    private File installationDir;

    private File deploymentDir;

    private File sharedLibDir;

    private File serviceAssembliesDir;

    private File tmpDir;

    private File logDir;

    private final Map envMap;

    private final AtomicBoolean started;

    public EnvironmentContext()
    {
        envMap = new ConcurrentHashMap();
        started = new AtomicBoolean( false );
    }

    public static String getVersion()
    {
        String answer = null;
        Package p = Package.getPackage( "com.sanxing.sesame" );
        if ( p != null )
        {
            answer = p.getImplementationVersion();
        }
        return ( ( answer != null ) ? answer : "" );
    }

    @Override
    public String getDescription()
    {
        return "Manages Environment for the Container";
    }

    public File getComponentsDir()
    {
        return componentsDir;
    }

    public File getInstallationDir()
    {
        return installationDir;
    }

    public void setInstallationDir( File installationDir )
    {
        this.installationDir = installationDir;
    }

    public File getDeploymentDir()
    {
        return deploymentDir;
    }

    public void setDeploymentDir( File deploymentDir )
    {
        this.deploymentDir = deploymentDir;
    }

    public File getSharedLibDir()
    {
        return sharedLibDir;
    }

    public File getTmpDir()
    {
        if ( tmpDir != null )
        {
            FileUtil.buildDirectory( tmpDir );
        }
        return tmpDir;
    }

    public File getLogDir()
    {
        if ( logDir != null )
        {
            FileUtil.buildDirectory( logDir );
        }
        return logDir;
    }

    public File getServiceAssembliesDir()
    {
        return serviceAssembliesDir;
    }

    @Override
    public void init( JBIContainer container )
        throws JBIException
    {
        super.init( container );
        jbiRootDir = new File( container.getRootDir() );
        buildDirectoryStructure();
    }

    @Override
    protected Class getServiceMBean()
    {
        return EnvironmentContextMBean.class;
    }

    @Override
    public void start()
        throws JBIException
    {
        if ( started.compareAndSet( false, true ) )
        {
            super.start();
        }
    }

    @Override
    public void stop()
        throws JBIException
    {
        if ( started.compareAndSet( true, false ) )
        {
            super.stop();
        }
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        super.shutDown();
        envMap.clear();
        container.getManagementContext().unregisterMBean( this );
    }

    public ComponentEnvironment registerComponent( ComponentMBeanImpl connector )
        throws JBIException
    {
        return registerComponent( null, connector );
    }

    public ComponentEnvironment registerComponent( ComponentEnvironment result, ComponentMBeanImpl connector )
        throws JBIException
    {
        if ( result == null )
        {
            result = new ComponentEnvironment();
        }
        if ( !( connector.isPojo() ) )
        {
            try
            {
                String name = connector.getComponentNameSpace().getName();
                if ( result.getComponentRoot() == null )
                {
                    File componentRoot = getComponentRootDir( name );
                    FileUtil.buildDirectory( componentRoot );
                    result.setComponentRoot( componentRoot );
                }
                if ( result.getWorkspaceRoot() == null )
                {
                    File privateWorkspace = createWorkspaceDirectory( name );
                    result.setWorkspaceRoot( privateWorkspace );
                }
                if ( result.getStateFile() == null )
                {
                    File stateFile = FileUtil.getDirectoryPath( result.getComponentRoot(), "state.xml" );
                    result.setStateFile( stateFile );
                }
            }
            catch ( IOException e )
            {
                throw new JBIException( e );
            }
        }
        result.setLocalConnector( connector );
        envMap.put( connector, result );
        return result;
    }

    public File getComponentRootDir( String componentName )
    {
        if ( getComponentsDir() == null )
        {
            return null;
        }
        return FileUtil.getDirectoryPath( getComponentsDir(), componentName );
    }

    public File createComponentRootDir( String componentName )
        throws IOException
    {
        if ( getComponentsDir() == null )
        {
            return null;
        }
        return FileUtil.getDirectoryPath( getComponentsDir(), componentName );
    }

    public File getNewComponentInstallationDir( String componentName )
        throws IOException
    {
        File result = getComponentRootDir( componentName );

        return FileVersionUtil.getNewVersionDirectory( result );
    }

    public File getComponentInstallationDir( String componentName )
        throws IOException
    {
        File result = getComponentRootDir( componentName );

        return FileVersionUtil.getLatestVersionDirectory( result );
    }

    public ComponentEnvironment getNewComponentEnvironment( String compName )
        throws IOException
    {
        File rootDir = FileUtil.getDirectoryPath( getComponentsDir(), compName );
        File instDir = FileVersionUtil.getNewVersionDirectory( rootDir );
        File workDir = FileUtil.getDirectoryPath( rootDir, "workspace" );
        File stateFile = FileUtil.getDirectoryPath( rootDir, "state.xml" );
        ComponentEnvironment env = new ComponentEnvironment();
        env.setComponentRoot( rootDir );
        env.setInstallRoot( instDir );
        env.setWorkspaceRoot( workDir );
        env.setStateFile( stateFile );
        return env;
    }

    public ComponentEnvironment getComponentEnvironment( String compName )
        throws IOException
    {
        File rootDir = FileUtil.getDirectoryPath( getComponentsDir(), compName );
        File instDir = FileVersionUtil.getLatestVersionDirectory( rootDir );
        File workDir = FileUtil.getDirectoryPath( rootDir, "workspace" );
        File stateFile = FileUtil.getDirectoryPath( rootDir, "state.xml" );
        ComponentEnvironment env = new ComponentEnvironment();
        env.setComponentRoot( rootDir );
        env.setInstallRoot( instDir );
        env.setWorkspaceRoot( workDir );
        env.setStateFile( stateFile );
        return env;
    }

    public ServiceAssemblyEnvironment getNewServiceAssemblyEnvironment( String saName )
        throws IOException
    {
        File rootDir = FileUtil.getDirectoryPath( getServiceAssembliesDir(), saName );
        File versDir = FileVersionUtil.getNewVersionDirectory( rootDir );
        File instDir = FileUtil.getDirectoryPath( versDir, "install" );
        File susDir = FileUtil.getDirectoryPath( versDir, "sus" );
        File stateFile = FileUtil.getDirectoryPath( rootDir, "state.xml" );
        ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
        env.setRootDir( rootDir );
        env.setInstallDir( instDir );
        env.setSusDir( susDir );
        env.setStateFile( stateFile );
        return env;
    }

    public ServiceAssemblyEnvironment getServiceAssemblyEnvironment( String saName )
    {
        File rootDir = FileUtil.getDirectoryPath( getServiceAssembliesDir(), saName );
        File versDir = FileVersionUtil.getLatestVersionDirectory( rootDir );
        File instDir = FileUtil.getDirectoryPath( versDir, "install" );
        File susDir = FileUtil.getDirectoryPath( versDir, "sus" );
        File stateFile = FileUtil.getDirectoryPath( rootDir, "state.xml" );
        ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
        env.setRootDir( rootDir );
        env.setInstallDir( instDir );
        env.setSusDir( susDir );
        env.setStateFile( stateFile );
        return env;
    }

    public File createWorkspaceDirectory( String componentName )
        throws IOException
    {
        File result = FileUtil.getDirectoryPath( getComponentsDir(), componentName );
        result = FileUtil.getDirectoryPath( result, "workspace" );
        FileUtil.buildDirectory( result );
        return result;
    }

    public void unreregister( ComponentMBeanImpl connector )
    {
        envMap.remove( connector );
    }

    public void removeComponentRootDirectory( String componentName )
    {
        File file = getComponentRootDir( componentName );
        if ( file != null )
        {
            if ( !( FileUtil.deleteFile( file ) ) )
            {
                LOG.warn( "Failed to remove directory structure for component [version]: " + componentName + " ["
                    + file.getName() + ']' );
            }
            else
            {
                LOG.info( "Removed directory structure for component [version]: " + componentName + " ["
                    + file.getName() + ']' );
            }
        }
    }

    public File createSharedLibraryDirectory( String name )
    {
        File result = FileUtil.getDirectoryPath( getSharedLibDir(), name );
        FileUtil.buildDirectory( result );
        return result;
    }

    public void removeSharedLibraryDirectory( String name )
    {
        File result = FileUtil.getDirectoryPath( getSharedLibDir(), name );
        FileUtil.deleteFile( result );
    }

    private void buildDirectoryStructure()
        throws JBIException
    {
        try
        {
            jbiRootDir = jbiRootDir.getCanonicalFile();
            if ( !( jbiRootDir.exists() ) )
            {
                if ( !( jbiRootDir.mkdirs() ) )
                {
                    throw new JBIException( "Directory could not be created: " + jbiRootDir.getCanonicalFile() );
                }
            }
            else if ( !( jbiRootDir.isDirectory() ) )
            {
                throw new JBIException( "Not a directory: " + jbiRootDir.getCanonicalFile() );
            }
            if ( installationDir == null )
            {
                installationDir = FileUtil.getDirectoryPath( jbiRootDir, "install" );
            }
            installationDir = installationDir.getCanonicalFile();
            if ( deploymentDir == null )
            {
                deploymentDir = FileUtil.getDirectoryPath( jbiRootDir, "deploy" );
            }
            deploymentDir = deploymentDir.getCanonicalFile();
            componentsDir = FileUtil.getDirectoryPath( jbiRootDir, "components" ).getCanonicalFile();
            tmpDir = FileUtil.getDirectoryPath( jbiRootDir, "temp" ).getCanonicalFile();
            sharedLibDir = FileUtil.getDirectoryPath( jbiRootDir, "sharedlibs" ).getCanonicalFile();
            serviceAssembliesDir = FileUtil.getDirectoryPath( jbiRootDir, "service-assemblies" ).getCanonicalFile();
            logDir = FileUtil.getDirectoryPath( jbiRootDir, "log" ).getCanonicalFile();

            FileUtil.buildDirectory( installationDir );
            FileUtil.buildDirectory( deploymentDir );
            FileUtil.buildDirectory( componentsDir );
            FileUtil.buildDirectory( tmpDir );
            FileUtil.buildDirectory( sharedLibDir );
            FileUtil.buildDirectory( serviceAssembliesDir );
            FileUtil.buildDirectory( logDir );
        }
        catch ( IOException e )
        {
            throw new JBIException( e );
        }
    }

    public File getJbiRootDir()
    {
        return jbiRootDir;
    }
}