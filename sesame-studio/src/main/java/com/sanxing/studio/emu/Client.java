package com.sanxing.studio.emu;

import java.io.IOException;

import org.jdom.Element;

public interface Client
    extends Runnable
{
    public abstract Element send( String code, Element data, int timeout )
        throws IOException;
}