package com.sanxing.adp.codegen;

import com.sanxing.adp.parser.CodeGenerator;
import com.sanxing.adp.parser.PortTypeInfo;

public class ClientProxyGenerator
    extends CodeGenerator
{
    private void generateClientProxy()
    {
        for ( PortTypeInfo portTypeInfo : getInterfaces() )
        {
            generateByTemplate( portTypeInfo, "com/sanxing/adp/codegen/proxy.vm", "ProxyImpl" );
        }
    }

    @Override
    public void generate()
        throws Exception
    {
        super.generate();
        generateClientProxy();
    }
}