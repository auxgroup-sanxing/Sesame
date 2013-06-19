package com.sanxing.sesame.mbean;

import javax.jbi.JBIException;
import javax.jbi.management.AdminServiceMBean;

public interface ManagementContextMBean
    extends AdminServiceMBean
{
    public abstract String startComponent( String componentName )
        throws JBIException;

    public abstract String stopComponent( String componentName )
        throws JBIException;

    public abstract String shutDownComponent( String componentName )
        throws JBIException;
}