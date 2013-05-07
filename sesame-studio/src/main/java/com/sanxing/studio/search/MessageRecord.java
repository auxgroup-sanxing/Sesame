package com.sanxing.studio.search;

import javax.wsdl.Message;

public class MessageRecord
    extends Record
    implements SearcherName
{
    private Message message;

    public MessageRecord( Message message )
    {
        this.message = message;
        addField( new Column( "message name", message.getQName().toString(), true ) );
    }

    public MessageRecord()
    {
    }

    @Override
    public String getSearcherName()
    {
        return "message";
    }

    @Override
    public Column getDescriptionColumn()
    {
        return null;
    }

    @Override
    public Column getNameColumn()
    {
        return getColumnByName( "message name" );
    }
}