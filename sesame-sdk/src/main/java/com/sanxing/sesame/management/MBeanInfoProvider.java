package com.sanxing.sesame.management;

import java.beans.PropertyChangeListener;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

public interface MBeanInfoProvider
{
    public abstract MBeanAttributeInfo[] getAttributeInfos()
        throws JMException;

    public abstract MBeanOperationInfo[] getOperationInfos()
        throws JMException;

    public abstract Object getObjectToManage();

    public abstract String getName();

    public abstract String getType();

    public abstract String getSubType();

    public abstract String getDescription();

    public abstract void setPropertyChangeListener( PropertyChangeListener listener );
}