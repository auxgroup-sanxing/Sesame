package com.sanxing.adp;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

public class SesameClient
{
    private ADPEngine engine;

    private static SesameClient instance = null;

    protected void setEngine( ADPEngine engine )
    {
        this.engine = engine;
    }

    public static SesameClient getInstance()
    {
        if ( instance == null )
        {
            instance = new SesameClient();
        }
        return instance;
    }

    public Source send( Source input, QName serviceName, QName interfaceName, QName operation )
    {
        return engine.send( input, serviceName, interfaceName, operation );
    }
}