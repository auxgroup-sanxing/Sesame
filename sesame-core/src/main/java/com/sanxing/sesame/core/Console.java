package com.sanxing.sesame.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Console
{
    private static boolean flag = true;

    private static List<OutputStream> streams = new ArrayList();

    private static final PrintStream systemOut = System.out;

    public static final PrintStream out = redirect( System.out, "System.out" );

    public static final PrintStream err = redirect( System.err, "System.err" );

    private static PrintStream redirect( OutputStream out, String filename )
    {
        final Logger logger = Logger.getLogger( filename );
        try
        {
            String logDir = System.getProperty( "SESAME_HOME" ) + File.separator + "logs";
            logger.setUseParentHandlers( false );
            FileHandler handler = new FileHandler( logDir + File.separator + filename, 52428800, 10, true );
            handler.setEncoding( System.getProperty( "file.encoding" ) );
            handler.setFormatter( new Formatter()
            {
                @Override
                public String format( LogRecord record )
                {
                    return record.getMessage();
                }
            } );
            logger.addHandler( handler );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }

        return new PrintStream( out, true )
        {
            @Override
            public void write( int b )
            {
                if ( Console.flag )
                {
                    super.write( b );
                }
            }

            @Override
            public void write( byte[] buf, int off, int len )
            {
                if ( Console.flag )
                {
                    super.write( buf, off, len );
                }

                logger.info( new String( buf, off, len ) );

                for ( OutputStream output : Console.streams )
                {
                    try
                    {
                        output.write( buf, off, len );
                    }
                    catch ( IOException e )
                    {
                    }
                }
            }
        };
    }

    public static void echo( boolean on )
    {
        flag = on;
    }

    public static void echo( String message )
    {
        systemOut.println( message );
    }

    public static void addOutput( OutputStream output )
    {
        streams.add( output );
    }

    public static void removeOutput( OutputStream output )
    {
        streams.remove( output );
    }
}