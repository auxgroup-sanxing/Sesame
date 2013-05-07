package com.sanxing.sesame.jmx.mbean.admin;

import java.io.FileInputStream;
import java.nio.ByteBuffer;

import com.sanxing.sesame.jmx.FilePackage;

public class FileServer
    implements FileServerMBean
{
    @Override
    public FilePackage transfer( FilePackage filePackage )
    {
        String fileName = filePackage.getFileName();
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( fileName );
            ByteBuffer buffer = ByteBuffer.allocate( (int) filePackage.getPageSize() );
            if ( filePackage.getCurrentPackage() > 1 )
            {
                buffer.flip();
                buffer.clear();
            }
            else
            {
                buffer.flip();
                byte[] temp = new byte[buffer.remaining()];
                buffer.get( temp );
                filePackage.setPackageData( temp );
                filePackage.setCurrentPackage( filePackage.getCurrentPackage() + 1 );

                if ( fis != null )
                {
                    try
                    {
                        fis.close();
                    }
                    catch ( Exception localException1 )
                    {
                    }
                }
            }
        }
        catch ( Exception e )
        {
        }
        finally
        {
            if ( fis != null )
            {
                try
                {
                    fis.close();
                }
                catch ( Exception localException2 )
                {
                }
            }
        }
        if ( fis != null )
        {
            try
            {
                fis.close();
            }
            catch ( Exception localException3 )
            {
            }
        }
        return filePackage;
    }

    @Override
    public String getDescription()
    {
        return "文件服务器";
    }
}