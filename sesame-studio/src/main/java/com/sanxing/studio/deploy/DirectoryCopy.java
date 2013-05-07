package com.sanxing.studio.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

public class DirectoryCopy
{
    FileInputStream FIS;

    FileOutputStream FOS;

    static String srcfile = "D:/aaa";

    static String desfile = "D:/bbb";

    public static boolean copyDirectory( String SrcDirectoryPath, String DesDirectoryPath )
    {
        try
        {
            File F0 = new File( DesDirectoryPath );
            if ( !( F0.exists() ) )
            {
                new File( DesDirectoryPath ).mkdirs();
            }
            File F = new File( SrcDirectoryPath );

            FilenameFilter filter = new FilenameFilter()
            {
                @Override
                public boolean accept( File dir, String name )
                {
                    return ( ( !( name.endsWith( ".svn" ) ) ) && ( !( name.endsWith( ".java" ) ) ) );
                }
            };
            File[] allFile = F.listFiles( filter );
            int totalNum = allFile.length;
            String srcName = "";
            String desName = "";
            int currentFile = 0;

            for ( currentFile = 0; currentFile < totalNum; ++currentFile )
            {
                if ( !( allFile[currentFile].isDirectory() ) )
                {
                    srcName = allFile[currentFile].toString();
                    desName = DesDirectoryPath + File.separator + allFile[currentFile].getName();

                    FileCopy FC = new FileCopy();
                    FC.copyFile( srcName, desName );
                }
                else
                {
                    if ( copyDirectory( allFile[currentFile].getPath().toString(), DesDirectoryPath + File.separator
                        + allFile[currentFile].getName().toString() ) )
                    {
                        continue;
                    }

                    throw new Exception( "SubDirectory Copy Error!" );
                }
            }

            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return false;
    }
}