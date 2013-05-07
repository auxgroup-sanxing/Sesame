package com.sanxing.studio.emu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class StreamChannel
{
    private final InputStream in;

    private final OutputStream out;

    public StreamChannel( InputStream in, OutputStream out )
    {
        this.in = in;
        this.out = out;
    }

    public InputStream getInput()
    {
        return in;
    }

    public OutputStream getOutput()
    {
        return out;
    }

    public abstract void close()
        throws IOException;
}