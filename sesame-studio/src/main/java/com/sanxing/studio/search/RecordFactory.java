package com.sanxing.studio.search;

public class RecordFactory
{
    public static Record createRecord( String type )
    {
        if ( type.equalsIgnoreCase( "element" ) )
        {
            return new ElementRecord();
        }
        if ( type.equalsIgnoreCase( "message" ) )
        {
            return new MessageRecord();
        }
        if ( type.equalsIgnoreCase( "operation" ) )
        {
            return new OperationRecord();
        }
        if ( type.equalsIgnoreCase( "interface" ) )
        {
            return new InterfaceRecord();
        }
        if ( type.equalsIgnoreCase( "service" ) )
        {
            return new ServiceRecord();
        }
        if ( type.equalsIgnoreCase( "project" ) )
        {
            return new ProjectRecord();
        }
        return new DummyRecord();
    }
}