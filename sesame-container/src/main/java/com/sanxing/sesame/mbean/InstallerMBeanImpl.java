package com.sanxing.sesame.mbean;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.InstallerMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.container.JBIContainer;

public class InstallerMBeanImpl
    implements InstallerMBean
{
    private static final Logger LOG = LoggerFactory.getLogger( InstallerMBeanImpl.class );

    private final InstallationContextImpl context;

    private final JBIContainer container;

    private ObjectName objectName;

    private ObjectName extensionMBeanName;

    private final Bootstrap bootstrap;

    private boolean initialized;

    private JarFileClassLoader bootstrapLoader;

    private JarFileClassLoader componentLoader;

    public InstallerMBeanImpl( JBIContainer container, InstallationContextImpl ic )
        throws DeploymentException
    {
        this.container = container;
        context = ic;
        bootstrap = createBootstrap();
        initBootstrap();
    }

    private void initBootstrap()
        throws DeploymentException
    {
        try
        {
            if ( !( initialized ) )
            {
                try
                {
                    if ( ( extensionMBeanName != null ) && ( container.getMBeanServer() != null )
                        && ( container.getMBeanServer().isRegistered( extensionMBeanName ) ) )
                    {
                        container.getMBeanServer().unregisterMBean( extensionMBeanName );
                    }
                }
                catch ( InstanceNotFoundException e )
                {
                }
                catch ( MBeanRegistrationException e )
                {
                }
                bootstrap.init( context );
                extensionMBeanName = bootstrap.getExtensionMBeanName();
                initialized = true;
            }
        }
        catch ( JBIException e )
        {
            LOG.error( "Could not initialize bootstrap", e );
            throw new DeploymentException( e );
        }
    }

    protected void cleanUpBootstrap()
        throws DeploymentException
    {
        try
        {
            bootstrap.cleanUp();
        }
        catch ( JBIException e )
        {
            throw new DeploymentException( e );
        }
        finally
        {
            initialized = false;
        }
    }

    private Bootstrap createBootstrap()
        throws DeploymentException
    {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        com.sanxing.sesame.deployment.Component descriptor = context.getDescriptor();
        try
        {
            bootstrapLoader =
                buildBootClassLoader( context.getInstallRootAsDir(),
                    descriptor.getBootstrapClassPath().getPathElements(),
                    descriptor.isBootstrapClassLoaderDelegationParentFirst(), null );

            Thread.currentThread().setContextClassLoader( bootstrapLoader );
            Class bootstrapClass = bootstrapLoader.loadClass( descriptor.getBootstrapClassName() );
            Bootstrap localBootstrap = (Bootstrap) bootstrapClass.newInstance();

            return localBootstrap;
        }
        catch ( MalformedURLException e )
        {
            throw new DeploymentException( e );
        }
        catch ( ClassNotFoundException e )
        {
            throw new DeploymentException( e );
        }
        catch ( InstantiationException e )
        {
            throw new DeploymentException( e );
        }
        catch ( IllegalAccessException e )
        {
            throw new DeploymentException( e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldCl );
        }
    }

    @Override
    public String getInstallRoot()
    {
        return context.getInstallRoot();
    }

    @Override
    public ObjectName install()
        throws JBIException
    {
        if ( isInstalled() )
        {
            throw new DeploymentException( "Component is already installed" );
        }
        initBootstrap();
        bootstrap.onInstall();

        ObjectName result = null;
        try
        {
            result = activateComponent();
            ComponentMBeanImpl lcc = container.getRegistry().getComponent( context.getComponentName() );
            lcc.persistRunningState();
            context.setInstall( false );
        }
        finally
        {
            cleanUpBootstrap();
        }
        return result;
    }

    public ObjectName activateComponent()
        throws JBIException
    {
        ObjectName result = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        com.sanxing.sesame.deployment.Component descriptor = context.getDescriptor();
        try
        {
            List classPaths = context.getClassPathElements();
            componentLoader =
                buildClassLoader( context.getInstallRootAsDir(),
                    (String[]) classPaths.toArray( new String[classPaths.size()] ),
                    descriptor.isComponentClassLoaderDelegationParentFirst(), context.getSharedLibraries() );

            Thread.currentThread().setContextClassLoader( componentLoader );
            Class componentClass = componentLoader.loadClass( descriptor.getComponentClassName() );
            javax.jbi.component.Component component = (javax.jbi.component.Component) componentClass.newInstance();
            result =
                container.getAdminCommandsService().activateComponent( context.getInstallRootAsDir(), component,
                    context.getComponentDescription(), (ComponentContextImpl) context.getContext(),
                    context.isBinding(), context.isEngine(), context.getSharedLibraries(), container );
        }
        catch ( MalformedURLException e )
        {
            throw new DeploymentException( e );
        }
        catch ( NoClassDefFoundError e )
        {
            throw new DeploymentException( e );
        }
        catch ( ClassNotFoundException e )
        {
            throw new DeploymentException( e );
        }
        catch ( InstantiationException e )
        {
            throw new DeploymentException( e );
        }
        catch ( IllegalAccessException e )
        {
            throw new DeploymentException( e );
        }
        catch ( JBIException e )
        {
            throw new DeploymentException( e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldCl );
        }
        return result;
    }

    @Override
    public boolean isInstalled()
    {
        return ( !( context.isInstall() ) );
    }

    @Override
    public void uninstall()
        throws JBIException
    {
        if ( !( isInstalled() ) )
        {
            throw new DeploymentException( "Component is not installed" );
        }
        String componentName = context.getComponentName();
        try
        {
            container.getAdminCommandsService().deactivateComponent( componentName );
            bootstrap.onUninstall();
            context.setInstall( true );
        }
        finally
        {
            cleanUpBootstrap();

            componentLoader.destroy();
            bootstrapLoader.destroy();

            System.gc();
            container.getEnvironmentContext().removeComponentRootDirectory( componentName );
        }
    }

    @Override
    public ObjectName getInstallerConfigurationMBean()
        throws JBIException
    {
        return extensionMBeanName;
    }

    public ObjectName getObjectName()
    {
        return objectName;
    }

    public void setObjectName( ObjectName objectName )
    {
        this.objectName = objectName;
    }

    private JarFileClassLoader buildClassLoader( File dir, String[] classPathNames, boolean parentFirst,
                                                 String[] shareLibNames )
        throws MalformedURLException, DeploymentException
    {
        ClassLoader[] parents = prepareParentClassLoader( shareLibNames );
        List urls = parseComponentClasspath( dir, classPathNames );
        JarFileClassLoader classLoader =
            new JarFileClassLoader( (URL[]) urls.toArray( new URL[urls.size()] ), parents, !( parentFirst ),
                new String[0], new String[] { "java.", "javax." } );

        if ( LOG.isTraceEnabled() )
        {
            LOG.trace( "Component class loader: " + classLoader );
        }
        return classLoader;
    }

    private JarFileClassLoader buildBootClassLoader( File dir, String[] classPathNames, boolean parentFirst,
                                                     String[] shareLibNames )
        throws MalformedURLException, DeploymentException
    {
        ClassLoader[] parents = prepareParentClassLoader( shareLibNames );
        List urls = parseComponentClasspath( dir, classPathNames );
        JarFileClassLoader classLoader =
            new JarFileClassLoader( (URL[]) urls.toArray( new URL[urls.size()] ), parents, !( parentFirst ),
                new String[0], new String[] { "java.", "javax." } );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Component class loader: " + classLoader );
        }
        return classLoader;
    }

    private ClassLoader[] prepareParentClassLoader( String[] shareLibNames )
        throws DeploymentException
    {
        ClassLoader[] parents;
        if ( ( shareLibNames != null ) && ( shareLibNames.length > 0 ) )
        {
            parents = new ClassLoader[shareLibNames.length + 1];
            for ( int i = 0; i < shareLibNames.length; ++i )
            {
                SharedLibrary sl = container.getRegistry().getSharedLibrary( shareLibNames[i] );

                if ( sl == null )
                {
                    throw new DeploymentException( "Shared library " + shareLibNames[i] + " is not installed" );
                }
                parents[i] = sl.getClassLoader();
            }
            parents[shareLibNames.length] = super.getClass().getClassLoader();
        }
        else
        {
            parents = new ClassLoader[] { super.getClass().getClassLoader() };
        }
        return parents;
    }

    private List<URL> parseComponentClasspath( File dir, String[] classPathNames )
        throws MalformedURLException
    {
        List urls = new ArrayList();
        for ( int i = 0; i < classPathNames.length; ++i )
        {
            File file = new File( dir, classPathNames[i] );
            if ( !( file.exists() ) )
            {
                LOG.warn( "Unable to add File " + file + " to class path as it doesn't exist: "
                    + file.getAbsolutePath() );
            }

            urls.add( file.toURL() );
        }

        File[] libs = new File( dir, "lib" ).listFiles( new FilenameFilter()
        {
            @Override
            public boolean accept( File dir, String name )
            {
                return name.endsWith( ".jar" );
            }
        } );
        if ( libs != null )
        {
            for ( File lib : libs )
            {
                urls.add( lib.toURL() );
            }
        }
        return urls;
    }
}