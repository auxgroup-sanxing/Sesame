package com.sanxing.sesame.mbean;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.deployment.ClassPath;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.sharelib.ShareLibCallback;

public class SharedLibrary
    implements SharedLibraryMBean, MBeanInfoProvider
{
    private final com.sanxing.sesame.deployment.SharedLibrary library;

    private final File installationDir;

    private ClassLoader classLoader;

    private ShareLibCallback callback;

    public SharedLibrary( com.sanxing.sesame.deployment.SharedLibrary library, File installationDir )
    {
        this.library = library;
        this.installationDir = installationDir;
        classLoader = createClassLoader();
        String strCallBackClazz = library.getCallbackClazz();
        if ( strCallBackClazz != null )
        {
            ClassLoader oldOne = Thread.currentThread().getContextClassLoader();
            try
            {
                Thread.currentThread().setContextClassLoader( classLoader );
                if ( ( library.getCallbackClazz() != null ) && ( library.getCallbackClazz().length() > 0 ) )
                {
                    ShareLibCallback callback =
                        (ShareLibCallback) classLoader.loadClass( library.getCallbackClazz() ).newInstance();

                    this.callback = callback;
                    callback.onInstall( installationDir );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( oldOne );
            }
        }
    }

    public void dispose()
    {
        ClassLoader oldOne = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( classLoader );
        if ( callback != null )
        {
            callback.onDispose( installationDir );
        }

        Thread.currentThread().setContextClassLoader( oldOne );

        if ( classLoader instanceof JarFileClassLoader )
        {
            ( (JarFileClassLoader) classLoader ).destroy();
        }
        classLoader = null;
    }

    public com.sanxing.sesame.deployment.SharedLibrary getLibrary()
    {
        return library;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    private ClassLoader createClassLoader()
    {
        boolean parentFirst = library.isParentFirstClassLoaderDelegation();

        ClassLoader parent = super.getClass().getClassLoader();

        ClassPath cp = library.getSharedLibraryClassPath();
        String[] classPathNames = cp.getPathElements();
        URL[] urls = new URL[classPathNames.length];
        for ( int i = 0; i < classPathNames.length; ++i )
        {
            File file = new File( installationDir, classPathNames[i] );
            try
            {
                urls[i] = file.toURL();
            }
            catch ( MalformedURLException e )
            {
                throw new IllegalArgumentException( classPathNames[i], e );
            }
        }
        return new JarFileClassLoader( urls, parent, !( parentFirst ), new String[0],
            new String[] { "java.", "javax." } );
    }

    @Override
    public String getDescription()
    {
        return library.getIdentification().getDescription();
    }

    @Override
    public String getName()
    {
        return library.getIdentification().getName();
    }

    @Override
    public String getVersion()
    {
        return library.getVersion();
    }

    @Override
    public Object getObjectToManage()
    {
        return this;
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "name", "name of the shared library" );

        helper.addAttribute( getObjectToManage(), "description", "description of this shared library" );

        helper.addAttribute( getObjectToManage(), "version", "version of this shared library" );

        return helper.getAttributeInfos();
    }

    @Override
    public MBeanOperationInfo[] getOperationInfos()
        throws JMException
    {
        return null;
    }

    @Override
    public String getSubType()
    {
        return null;
    }

    @Override
    public String getType()
    {
        return "SharedLibrary";
    }

    @Override
    public void setPropertyChangeListener( PropertyChangeListener l )
    {
    }
}