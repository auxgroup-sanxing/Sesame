package com.sanxing.sesame.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{
    public static byte[] zipBytes( byte[] bytes )
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream( output );
        try
        {
            ZipEntry temp = new ZipEntry( "" + Randomizer.getInstance().nextLong() );
            zout.putNextEntry( temp );
            zout.write( bytes );
            zout.closeEntry();
            zout.flush();
            bytes = output.toByteArray();
            return bytes;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return bytes;
        }
        finally
        {
            try
            {
                zout.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    public static byte[] zipBytes( File file )
    {
        byte[] content = new byte[(int)file.length()];
        InputStream in;
        try
        {
            in = new FileInputStream( file );
            in.read( content );
            in.close();
            return zipBytes( content );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        return content;
    }
    
    public static byte[] unzipBytes( byte[] src )
    {
        ByteArrayInputStream input = new ByteArrayInputStream( src );
        ZipInputStream zin = new ZipInputStream( input );
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            zin.getNextEntry();
            byte[] temp = new byte[10240];
            int i = 0;
            while ( ( i = zin.read( temp ) ) != -1 )
            {
                output.write( temp, 0, i );
            }
            zin.closeEntry();
            return output.toByteArray();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return src;
        }
        finally
        {
            try
            {
                zin.close();
                output.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    public static void unzipBytes( byte[] src, OutputStream output )
    {
        ByteArrayInputStream input = new ByteArrayInputStream( src );
        ZipInputStream zin = new ZipInputStream( input );
        try
        {
            zin.getNextEntry();
            byte[] temp = new byte[10240];
            int i = 0;
            while ( ( i = zin.read( temp ) ) != -1 )
            {
                output.write( temp, 0, i );
                output.flush();
            }
            zin.closeEntry();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                zin.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
}