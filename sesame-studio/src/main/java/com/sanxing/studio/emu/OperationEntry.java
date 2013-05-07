package com.sanxing.studio.emu;

import org.apache.ws.commons.schema.XmlSchema;
import org.jdom.Document;

public class OperationEntry
{
    private XmlSchema schema;

    private Document data;

    private String interfaceName;

    public Document getData()
    {
        return data;
    }

    public void setData( Document data )
    {
        this.data = data;
    }

    public void setSchema( XmlSchema schema )
    {
        this.schema = schema;
    }

    public XmlSchema getSchema()
    {
        return schema;
    }

    public void setInterfaceName( String interfaceName )
    {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }
}