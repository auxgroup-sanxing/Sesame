package com.sanxing.adp.parser;

import java.util.HashMap;
import java.util.Map;

public class FaultInfo
{
    private String name;

    private final Map<String, PartInfo> parts = new HashMap();

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void addPart( PartInfo part )
    {
        parts.put( part.getName(), part );
    }

    public Map<String, PartInfo> getParts()
    {
        return parts;
    }
}