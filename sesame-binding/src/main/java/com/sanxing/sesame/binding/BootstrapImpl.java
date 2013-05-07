package com.sanxing.sesame.binding;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.sanxing.sesame.exception.NotInitialisedYetException;

public class BootstrapImpl
    implements Bootstrap
{
    private static final Logger LOG = LoggerFactory.getLogger( BootstrapImpl.class );

    private InstallationContext installContext;

    private ObjectName extensionMBeanName;

    @Override
    public void cleanUp()
        throws JBIException
    {
    }

    @Override
    public ObjectName getExtensionMBeanName()
    {
        return extensionMBeanName;
    }

    @Override
    public void init( InstallationContext ctx )
        throws JBIException
    {
        installContext = ctx;
    }

    @Override
    public void onInstall()
        throws JBIException
    {
        if ( installContext == null )
        {
            throw new NotInitialisedYetException();
        }
        DocumentFragment fragment = installContext.getInstallationDescriptorExtension();
        if ( fragment != null )
        {
            LOG.debug( "Installation Descriptor Extension Found" );
        }
        else
        {
            LOG.debug( "Installation Descriptor Extension Not Found !" );
        }
    }

    @Override
    public void onUninstall()
        throws JBIException
    {
    }
}