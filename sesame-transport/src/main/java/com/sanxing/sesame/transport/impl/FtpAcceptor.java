package com.sanxing.sesame.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.core.naming.JNDIUtil;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.util.SesameFTPClient;

public class FtpAcceptor
    extends TaskTransport
{
    private static Logger LOG = LoggerFactory.getLogger( FtpAcceptor.class );

    protected void write( OutputStream output, byte[] bytes, int length )
        throws IOException
    {
    }

    @Override
    public String getCharacterEncoding()
    {
        return encoding;
    }

    public String getDescription()
    {
        return "ftp transport";
    }

    @Override
    public void reply( MessageContext context )
        throws IOException
    {
        String path = context.getPath();
        Map props = bindingPropsCache.get( path );

        BinaryResult result = (BinaryResult) context.getResult();
        byte[] resp = result.getBytes();
        SesameFTPClient ftpClient;
        if ( resp != null )
        {
            ftpClient = new SesameFTPClient();
            try
            {
                ftpClient.setURI( uri );
                ftpClient.setProperties( properties );
                ftpClient.setBindingProperties( props );
                if ( !( ftpClient.connectAddLogin() ) )
                {
                    throw new IOException( "can not connect and login to ftp server." );
                }
                if ( ftpClient.isConnected() )
                {
                    if ( !( ftpClient.uploadFile( context.getAction() + "_response", new ByteArrayInputStream( resp ) ) ) )
                    {
                        LOG.debug( "send response error!" );
                    }
                    ftpClient.logoutAddDisconnect();
                }
                else
                {
                    LOG.debug( "can not connect to ftp server." );
                }
            }
            catch ( IOException ioe )
            {
                LOG.debug( ioe.getMessage(), ioe );
                throw ioe;
            }
            catch ( Exception e )
            {
                LOG.debug( e.getMessage(), e );
                throw new IOException( e.getMessage() );
            }
            finally
            {
                ftpClient.disconnect();
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
        catch ( SchedulerException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
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
        catch ( SchedulerException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void executeTask( Map<?, ?> properties )
    {
        SesameFTPClient ftpClient = new SesameFTPClient();
        try
        {
            ftpClient.setURI( uri );
            ftpClient.setProperties( this.properties );
            ftpClient.setBindingProperties( properties );
            if ( !( ftpClient.connectAddLogin() ) )
            {
                throw new IOException( "can not connect and login to ftp server[" + uri.getAuthority() + "]." );
            }
            if ( ftpClient.isConnected() )
            {
                Object[] names = ftpClient.getFileNames();
                LOG.debug( "filelist size is:" + names.length );
                for ( Object filename : names )
                {
                    if ( ftpClient.moveFile( (String) filename ) )
                    {
                        InputStream in = null;
                        in = ftpClient.processOneFile( (String) filename );
                        if ( in != null )
                        {
                            accept( in, (String) filename, (String) properties.get( "jobName" ) );

                            continue;
                        }

                        throw new Exception( "get file content error!file is:[" + filename + "]." );
                    }

                    LOG.debug( "can not move file:[" + filename + "]" );
                }

                ftpClient.logoutAddDisconnect();
            }
            else
            {
                LOG.debug( "can not connect to ftp server[" + uri.getAuthority() + "]." );
            }
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
        }
        finally
        {
            try
            {
                ftpClient.disconnect();
            }
            catch ( IOException e )
            {
                LOG.debug( e.getMessage(), e );
            }
        }
    }

    private void insertBatch( String fileName )
        throws Exception
    {
        InitialContext context = JNDIUtil.getInitialContext();
        DataSource data = (DataSource) context.lookup( "sesame-ds" );
        Connection conn = data.getConnection();
        String sql = "insert into fileregister(filename,successtotal,failuretotal) values(?,?,?)";
        PreparedStatement sm = conn.prepareStatement( sql );
        sm.setString( 1, fileName );
        sm.setInt( 2, 1 );
        sm.setInt( 3, 3 );
        sm.execute();

        sm.close();

        conn.close();
    }
}