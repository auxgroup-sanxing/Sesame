package com.sanxing.sesame.engine.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sanxing.sesame.engine.FlowInfo;

public class ProcessRegistry
{
    private final Map<String, FlowInfo> flows = new ConcurrentHashMap();

    public void put( String operationName, FlowInfo flow )
    {
        flows.put( operationName, flow );
    }

    public FlowInfo get( String operationName )
    {
        return flows.get( operationName );
    }

    public void clear()
    {
        flows.clear();
    }
}