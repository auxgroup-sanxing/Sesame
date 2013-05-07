package com.sanxing.sesame.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.jar.JarFile;

public final class IoUtil
{
    public static byte[] getBytes( InputStream inputStream )
        throws IOException
    {
        try
        {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for ( int count = inputStream.read( buffer ); count >= 0; count = inputStream.read( buffer ) )
            {
                out.write( buffer, 0, count );
            }
            byte[] bytes = out.toByteArray();
            return bytes;
        }
        finally
        {
            close( inputStream );
        }
    }

    public static void flush( OutputStream thing )
    {
        if ( thing == null )
        {
            return;
        }
        try
        {
            thing.flush();
        }
        catch ( Exception localException )
        {
        }
    }

    public static void flush( Writer thing )
    {
        if ( thing == null )
        {
            return;
        }
        try
        {
            thing.flush();
        }
        catch ( Exception localException )
        {
        }
    }

    public static void close( JarFile thing )
    {
        if ( thing == null )
        {
            return;
        }
        try
        {
            thing.close();
        }
        catch ( Exception localException )
        {
        }
    }

    public static void close( InputStream thing )
    {
        if ( thing == null )
        {
            return;
        }
        try
        {
            thing.close();
        }
        catch ( Exception localException )
        {
        }
    }

    public static void close( OutputStream thing )
    {
        if ( thing == null )
        {
            return;
        }
        try
        {
            thing.close();
        }
        catch ( Exception localException )
        {
        }
    }

    public static void close( Reader thing )
    {
        if ( thing == null )
        {
            return;
        }
        try
        {
            thing.close();
        }
        catch ( Exception localException )
        {
        }
    }

    public static void close( Writer thing )
    {
        if ( thing == null )
        {
            return;
        }
        try
        {
            thing.close();
        }
        catch ( Exception localException )
        {
        }
    }

    public static final class EmptyInputStream
        extends InputStream
    {
        @Override
        public int read()
        {
            return -1;
        }

        @Override
        public int read( byte[] b )
        {
            return -1;
        }

        @Override
        public int read( byte[] b, int off, int len )
        {
            return -1;
        }

        @Override
        public long skip( long n )
        {
            return 0L;
        }

        @Override
        public int available()
        {
            return 0;
        }

        @Override
        public void close()
        {
        }

        @Override
        public synchronized void mark( int readlimit )
        {
        }

        @Override
        public synchronized void reset()
        {
        }

        @Override
        public boolean markSupported()
        {
            return false;
        }
    }
}