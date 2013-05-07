package com.sanxing.adp;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

import com.sanxing.sesame.exception.NotInitialisedYetException;

public class BootstrapImpl
    implements Bootstrap
{
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
    }

    @Override
    public void onUninstall()
        throws JBIException
    {
    }
}