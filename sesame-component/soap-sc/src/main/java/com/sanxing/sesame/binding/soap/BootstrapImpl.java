package com.sanxing.sesame.binding.soap;

import com.sanxing.sesame.exception.NotInitialisedYetException;
import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DocumentFragment;

public class BootstrapImpl
    implements Bootstrap
{
    private static final Log LOG = LogFactory.getLog( BootstrapImpl.class );

    private InstallationContext installContext;

    private ObjectName extensionMBeanName;

    public void cleanUp()
        throws JBIException
    {
    }

    public ObjectName getExtensionMBeanName()
    {
        return this.extensionMBeanName;
    }

    public void init( InstallationContext ctx )
        throws JBIException
    {
        this.installContext = ctx;
    }

    public void onInstall()
        throws JBIException
    {
        if ( this.installContext == null )
        {
            throw new NotInitialisedYetException();
        }
        DocumentFragment fragment = this.installContext.getInstallationDescriptorExtension();
        if ( fragment != null )
            LOG.debug( "Installation Descriptor Extension Found" );
        else
            LOG.debug( "Installation Descriptor Extension Not Found !" );
    }

    public void onUninstall()
        throws JBIException
    {
    }
}