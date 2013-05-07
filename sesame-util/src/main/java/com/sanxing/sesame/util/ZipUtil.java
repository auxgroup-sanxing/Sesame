package com.sanxing.sesame.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{
    public static byte[] zipObject( byte[] textObj )
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream( output );
        byte[] arrayOfByte;
        try
        {
            ZipEntry temp = new ZipEntry( "" + Randomizer.getInstance().nextLong() );
            zout.putNextEntry( temp );
            zout.write( textObj );
            zout.closeEntry();
            zout.flush();
            textObj = output.toByteArray();
            return textObj;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return textObj;
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

    public static byte[] unzipTextObj( byte[] src )
    {
        ByteArrayInputStream input = new ByteArrayInputStream( src );
        ZipInputStream zin = new ZipInputStream( input );
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] arrayOfByte1;
        try
        {
            ZipEntry entry = zin.getNextEntry();
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
}