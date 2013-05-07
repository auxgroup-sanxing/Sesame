package com.sanxing.sesame.binding;

import java.util.Map;

import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FaultHandler;

public class Codec
{
    private Decoder decoder;

    private Encoder encoder;

    private FaultHandler faultHandler;

    private Map<String, String> properties;

    public Decoder getDecoder()
    {
        return decoder;
    }

    public void setDecoder( Decoder decoder )
    {
        this.decoder = decoder;
    }

    public Encoder getEncoder()
    {
        return encoder;
    }

    public void setEncoder( Encoder encoder )
    {
        this.encoder = encoder;
    }

    public FaultHandler getFaultHandler()
    {
        return faultHandler;
    }

    public void setFaultHandler( FaultHandler faultHandler )
    {
        this.faultHandler = faultHandler;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties( Map<String, String> properties )
    {
        this.properties = properties;
    }
}