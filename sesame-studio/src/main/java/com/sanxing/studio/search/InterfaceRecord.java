package com.sanxing.studio.search;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.PortType;

public class InterfaceRecord
    extends Record
    implements SearcherName
{
    private Port port;

    private Binding binding;

    private PortType type;

    public InterfaceRecord( Port port, Binding binding, PortType type )
    {
        this.port = port;
        this.binding = binding;
        this.type = type;
        addField( new Column( "port name", port.getName(), true ) );

        if ( binding != null )
        {
            addField( new Column( "binding name", binding.getQName().toString(), true ) );
        }

        if ( type != null )
        {
            addField( new Column( "interface name", type.getQName().toString(), true ) );
        }
    }

    public InterfaceRecord()
    {
    }

    @Override
    public String getSearcherName()
    {
        return "interface";
    }

    @Override
    public Column getDescriptionColumn()
    {
        return null;
    }

    @Override
    public Column getNameColumn()
    {
        return getColumnByName( "interface name" );
    }
}