package com.sanxing.studio.deploy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileCopy
{
    FileInputStream FIS;

    FileOutputStream FOS;

    public boolean copyFile( String src, String des )
    {
        try
        {
            FIS = new FileInputStream( src );
            FOS = new FileOutputStream( des );
            byte[] bt = new byte[4096];
            int readNum = 0;
            while ( ( readNum = FIS.read( bt ) ) != -1 )
            {
                FOS.write( bt, 0, readNum );
            }
            FIS.close();
            FOS.close();

            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
        finally
        {
            try
            {
                FIS.close();
                FOS.close();
            }
            catch ( IOException f )
            {
                f.printStackTrace();
            }
        }
    }
}