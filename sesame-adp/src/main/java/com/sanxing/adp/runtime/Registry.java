package com.sanxing.adp.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sanxing.adp.parser.PortTypeInfo;

public class Registry
{
    private static Registry instance;

    private final Map<QName, PortTypeInfo> interfaces = new HashMap();

    public PortTypeInfo getPortypeInfo( QName portypeName )
    {
        PortTypeInfo portTypeInfo = interfaces.get( portypeName );
        if ( portTypeInfo == null )
        {
            throw new RuntimeException( "unkown portype :[" + portypeName + "], are you registered it?" );
        }
        return portTypeInfo;
    }

    public static Registry getInstance()
    {
        if ( instance == null )
        {
            instance = new Registry();
        }
        return instance;
    }

    public void registerInfterface( PortTypeInfo portypeInfo )
    {
        interfaces.put( portypeInfo.getName(), portypeInfo );
    }
}