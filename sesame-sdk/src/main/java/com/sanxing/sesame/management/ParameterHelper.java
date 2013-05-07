package com.sanxing.sesame.management;

import javax.management.MBeanParameterInfo;

public class ParameterHelper
{
    private final MBeanParameterInfo[] infos;

    ParameterHelper( MBeanParameterInfo[] infos )
    {
        this.infos = infos;
    }

    public void setDescription( int index, String name, String description )
    {
        MBeanParameterInfo old = infos[index];
        infos[index] = new MBeanParameterInfo( name, old.getType(), description );
    }
}