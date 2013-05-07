package com.sanxing.sesame.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.util.SesameSFTPClient;

public class SFTPAcceptor
    extends TaskTransport
{
    private static Logger LOG = LoggerFactory.getLogger( SFTPAcceptor.class );

    @Override
    public void reply( MessageContext context )
        throws IOException
    {
        String path = context.getPath();
        Map props = bindingPropsCache.get( path );

        BinaryResult result = (BinaryResult) context.getResult();
        byte[] responseByte = result.getBytes();
        if ( responseByte != null )
        {
            SesameSFTPClient sftpClient = new SesameSFTPClient();
            try
            {
                sftpClient.setURI( uri );
                sftpClient.setProperties( properties );
                sftpClient.setBindingProperties( props );
                sftpClient.init();
                LOG.debug( "reply message is:" + new String( responseByte ) );

                sftpClient.putFile( context.getAction() + "_response", responseByte );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    sftpClient.release();
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void close()
        throws IOException
    {
        try
        {
            scheduler.shutdown();
            workExecutor.shutdown();
            active = false;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public String getCharacterEncoding()
    {
        return "utf-8";
    }

    public String getDescription()
    {
        return "SFTP Transport";
    }

    @Override
    public void open()
        throws IOException
    {
        try
        {
            active = true;

            scheduler = new SesameScheduler();
            scheduler.init();
            scheduler.start();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void executeTask( Map<?, ?> properties )
        throws Exception
    {
        SesameSFTPClient sftpClient = new SesameSFTPClient();
        try
        {
            sftpClient.setURI( uri );
            sftpClient.setProperties( this.properties );
            sftpClient.setBindingProperties( properties );
            sftpClient.init();

            FileObject[] files = sftpClient.getFiles();
            for ( FileObject file : files )
            {
                if ( ( file.getType() != FileType.FILE ) || ( !( sftpClient.moveFile( file ) ) ) )
                {
                    continue;
                }
                InputStream in = null;
                String filename = file.getName().getBaseName();
                LOG.debug( "process file:" + filename );
                in = sftpClient.processOneFile( filename );
                accept( in, filename, (String) properties.get( "jobName" ) );
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                sftpClient.release();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
}