package com.sanxing.sesame.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public final class XmlPersistenceSupport
{
    private static XStream xstream = new XStream( new DomDriver() );

    public static Object read( File file )
        throws IOException
    {
        Reader r = new FileReader( file );
        try
        {
            return xstream.fromXML( r );
        }
        finally
        {
            r.close();
        }
    }

    public static void write( File file, Object obj )
        throws IOException
    {
        Writer w = new FileWriter( file );
        try
        {
            xstream.toXML( obj, w );
        }
        finally
        {
            w.close();
        }
    }
}