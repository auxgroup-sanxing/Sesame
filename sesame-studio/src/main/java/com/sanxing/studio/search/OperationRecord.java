package com.sanxing.studio.search;

import javax.wsdl.Operation;

public class OperationRecord
    extends Record
    implements SearcherName
{
    private Operation operation;

    public OperationRecord( Operation operation )
    {
        this.operation = operation;
        if ( operation.getDocumentationElement() != null )
        {
            addField( new Column( "operation description", operation.getDocumentationElement().getTextContent(), true ) );
        }

        addField( new Column( "operation name", operation.getName(), true ) );
    }

    public OperationRecord()
    {
    }

    @Override
    public String getSearcherName()
    {
        return "operation";
    }

    @Override
    public Column getDescriptionColumn()
    {
        return getColumnByName( "operation description" );
    }

    @Override
    public Column getNameColumn()
    {
        return getColumnByName( "operation name" );
    }
}