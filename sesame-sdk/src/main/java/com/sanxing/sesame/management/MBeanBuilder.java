package com.sanxing.sesame.management;

import java.util.concurrent.ExecutorService;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.StandardMBean;

public final class MBeanBuilder
{
    public static DynamicMBean buildStandardMBean( Object theObject, Class interfaceMBean, String description,
                                                   ExecutorService executorService )
        throws JMException
    {
        DynamicMBean result = null;
        if ( theObject != null )
        {
            if ( theObject instanceof MBeanInfoProvider )
            {
                MBeanInfoProvider info = (MBeanInfoProvider) theObject;
                result =
                    new BaseStandardMBean( info.getObjectToManage(), interfaceMBean, description,
                        info.getAttributeInfos(), info.getOperationInfos(), executorService );
                info.setPropertyChangeListener( (BaseStandardMBean) result );
            }
            else
            {
                return new StandardMBean( theObject, interfaceMBean );
            }
        }
        return result;
    }
}