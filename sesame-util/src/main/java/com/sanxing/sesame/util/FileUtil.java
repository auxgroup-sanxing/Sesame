package com.sanxing.sesame.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class FileUtil
{
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static void moveFile( File src, File targetDirectory )
        throws IOException
    {
        if ( !( src.renameTo( new File( targetDirectory, src.getName() ) ) ) )
        {
            throw new IOException( "Failed to move " + src + " to " + targetDirectory );
        }
    }

    public static File getDirectoryPath( File parent, String subDirectory )
    {
        File result = null;
        if ( parent != null )
        {
            result = new File( parent, subDirectory );
        }
        return result;
    }

    public static boolean buildDirectory( File file )
    {
        return ( ( file.exists() ) || ( file.mkdirs() ) );
    }

    public static int countFilesInDirectory( File directory )
    {
        int count = 0;
        for ( File file : directory.listFiles() )
        {
            if ( file.isFile() )
            {
                ++count;
            }
            if ( file.isDirectory() )
            {
                count += countFilesInDirectory( file );
            }
        }
        return count;
    }

    public static void copyInputStream( InputStream in, OutputStream out )
        throws IOException
    {
        byte[] buffer = new byte[4096];
        int len = in.read( buffer );
        while ( len >= 0 )
        {
            out.write( buffer, 0, len );
            len = in.read( buffer );
        }
        in.close();
        out.close();
    }

    public static File unpackArchive( File theFile, File targetDir )
        throws IOException
    {
        if ( !( theFile.exists() ) )
        {
            throw new IOException( theFile.getAbsolutePath() + " does not exist" );
        }
        if ( !( buildDirectory( targetDir ) ) )
        {
            throw new IOException( "Could not create directory: " + targetDir );
        }

        ZipFile zipFile = new ZipFile( theFile );
        for ( Enumeration entries = zipFile.entries(); entries.hasMoreElements(); )
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File file = new File( targetDir, File.separator + entry.getName() );

            if ( !( buildDirectory( file.getParentFile() ) ) )
            {
                throw new IOException( "Could not create directory: " + file.getParentFile() );
            }
            if ( !( entry.isDirectory() ) )
            {
                copyInputStream( zipFile.getInputStream( entry ),
                    new BufferedOutputStream( new FileOutputStream( file ) ) );
            }
            else if ( !( buildDirectory( file ) ) )
            {
                throw new IOException( "Could not create directory: " + file );
            }
        }

        zipFile.close();
        return theFile;
    }

    public static File unpackArchive( URL url, File targetDir )
        throws IOException
    {
        if ( !( targetDir.exists() ) )
        {
            targetDir.mkdirs();
        }
        InputStream in = new BufferedInputStream( url.openStream(), 4096 );

        File zip = File.createTempFile( "src", ".zip", targetDir );
        OutputStream out = new BufferedOutputStream( new FileOutputStream( zip ) );
        copyInputStream( in, out );
        out.close();
        return unpackArchive( zip, targetDir );
    }

    public static boolean archiveContainsEntry( File theFile, String name )
        throws IOException
    {
        boolean result = false;

        ZipFile zipFile = new ZipFile( theFile );
        for ( Enumeration entries = zipFile.entries(); entries.hasMoreElements(); )
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if ( entry.getName().equals( name ) )
            {
                result = true;
                break;
            }
        }
        zipFile.close();
        return result;
    }

    public static synchronized File createUniqueDirectory( File rootDir, String seed )
        throws IOException
    {
        int index = seed.lastIndexOf( 46 );
        if ( index > 0 )
        {
            seed = seed.substring( 0, index );
        }
        File result = null;
        int count = 0;
        while ( result == null )
        {
            String name = seed + "." + count + ".tmp";
            File file = new File( rootDir, name );
            if ( !( file.exists() ) )
            {
                file.mkdirs();
                result = file;
            }
            ++count;
        }
        return result;
    }

    public static boolean deleteFile( File fileToDelete )
    {
        if ( ( fileToDelete == null ) || ( !( fileToDelete.exists() ) ) )
        {
            return true;
        }
        boolean result = true;
        if ( fileToDelete.isDirectory() )
        {
            File[] files = fileToDelete.listFiles();
            if ( files == null )
            {
                result = false;
            }
            else
            {
                for ( int i = 0; i < files.length; ++i )
                {
                    File file = files[i];
                    if ( file.getName().equals( "." ) )
                    {
                        continue;
                    }
                    if ( file.getName().equals( ".." ) )
                    {
                        continue;
                    }
                    if ( file.isDirectory() )
                    {
                        result &= deleteFile( file );
                    }
                    else
                    {
                        result &= file.delete();
                    }
                }
            }
        }
        result &= fileToDelete.delete();
        return result;
    }

    public static boolean deleteFileIfNotMatch( File fileToDelete, String[] regexs )
    {
        if ( ( fileToDelete == null ) || ( !( fileToDelete.exists() ) ) )
        {
            return true;
        }
        boolean result = true;
        int index = 0;
        if ( fileToDelete.isDirectory() )
        {
            File[] files = fileToDelete.listFiles();
            if ( files == null )
            {
                result = false;
            }
            else
            {
                for ( int i = 0; i < files.length; ++i )
                {
                    File file = files[i];
                    for ( index = 0; index < regexs.length; ++index )
                    {
                        if ( file.getName().matches( regexs[index] ) )
                        {
                            break;
                        }
                    }
                    if ( index < regexs.length )
                    {
                        continue;
                    }
                    if ( file.isDirectory() )
                    {
                        result &= deleteFileIfNotMatch( file, regexs );
                    }
                    else
                    {
                        result &= file.delete();
                    }
                }
            }
        }
        else
        {
            result &= fileToDelete.delete();
        }
        return result;
    }

    public static void zipDir( String directory, String zipName )
        throws IOException
    {
        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zipName ) );
        String path = "";
        zipDir( directory, zos, path );

        zos.close();
    }

    public static void zipDir( String directory, ZipOutputStream zos, String path )
        throws IOException
    {
        File zipDir = new File( directory );

        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[2156];
        int bytesIn = 0;

        for ( int i = 0; i < dirList.length; ++i )
        {
            File f = new File( zipDir, dirList[i] );
            if ( f.isDirectory() )
            {
                String filePath = f.getPath();
                zipDir( filePath, zos, path + f.getName() + "/" );
            }
            else
            {
                FileInputStream fis = new FileInputStream( f );
                try
                {
                    ZipEntry anEntry = new ZipEntry( path + f.getName() );
                    zos.putNextEntry( anEntry );
                    bytesIn = fis.read( readBuffer );
                    while ( bytesIn != -1 )
                    {
                        zos.write( readBuffer, 0, bytesIn );
                        bytesIn = fis.read( readBuffer );
                    }
                }
                finally
                {
                    fis.close();
                }
            }
        }
    }
}