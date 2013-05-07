package com.sanxing.sesame.jaxp;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class DOMStreamWriter
    implements XMLStreamWriter
{
    @Override
    public void close()
        throws XMLStreamException
    {
    }

    @Override
    public void flush()
        throws XMLStreamException
    {
    }
}