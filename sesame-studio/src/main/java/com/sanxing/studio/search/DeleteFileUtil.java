package com.sanxing.studio.search;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileUtil
{
    static Logger logger = LoggerFactory.getLogger( DeleteFileUtil.class.getName() );

    public static boolean delete( String fileName )
    {
        File file = new File( fileName );
        if ( !( file.exists() ) )
        {
            logger.debug( "删除文件失败：" + fileName + "文件不存在" );
            return false;
        }
        if ( file.isFile() )
        {
            return deleteFile( fileName );
        }
        return deleteDirectory( fileName );
    }

    public static boolean deleteFile( String fileName )
    {
        File file = new File( fileName );
        if ( ( file.isFile() ) && ( file.exists() ) )
        {
            file.delete();
            return true;
        }
        logger.debug( "删除单个文件" + fileName + "失败！" );
        return false;
    }

    public static boolean deleteDirectory( String dir )
    {
        if ( !( dir.endsWith( File.separator ) ) )
        {
            dir = dir + File.separator;
        }
        File dirFile = new File( dir );
        if ( ( !( dirFile.exists() ) ) || ( !( dirFile.isDirectory() ) ) )
        {
            logger.debug( "删除目录失败" + dir + "目录不存在！" );
            return false;
        }
        boolean flag = true;

        File[] files = dirFile.listFiles();
        for ( int i = 0; i < files.length; ++i )
        {
            if ( files[i].isFile() )
            {
                flag = deleteFile( files[i].getAbsolutePath() );
                if ( flag )
                {
                    continue;
                }
                break;
            }

            flag = deleteDirectory( files[i].getAbsolutePath() );
            if ( !( flag ) )
            {
                break;
            }

        }

        if ( !( flag ) )
        {
            logger.debug( "删除目录失败" );
            return false;
        }

        if ( dirFile.delete() )
        {
            return true;
        }
        logger.debug( "删除目录" + dir + "失败！" );
        return false;
    }
}