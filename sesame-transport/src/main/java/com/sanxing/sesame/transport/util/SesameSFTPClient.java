package com.sanxing.sesame.transport.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

public class SesameSFTPClient
{
    private String sftpBasePath;

    private URI uri;

    private String host = "10.2.210.97";

    private int port = 22;

    private String username = "sesame";

    private String password = "password";

    private String remoteGetPath = "/home/sesame/sftptest/get";

    private String remotePutPath = "/home/sesame/sftptest/put";

    private String remoteBakPath = "/home/sesame/sftptest/bak";

    private String localBakPath = "c:/sftptest/bak";

    private String localBak = "true";

    private FileSystemManager fsManager;

    private FileSystemOptions opts;

    private FileObject src = null;

    public void setURI( URI uri )
    {
        this.uri = uri;
    }

    public void setConnectorProperties( Map<?, ?> props )
    {
        setProperties( props );
        setBindingProperties( props );
    }

    public void setBindingProperties( Map<?, ?> props )
    {
        remoteGetPath = ( (String) props.get( "remoteGetPath" ) );
        remotePutPath = ( (String) props.get( "remotePutPath" ) );
        remoteBakPath = ( (String) props.get( "remoteBakPath" ) );
        localBakPath = ( (String) props.get( "localBakPath" ) );
        localBak = ( (String) props.get( "localBak" ) );
    }

    public void setProperties( Map<?, ?> props )
    {
        host = uri.getHost();
        port = uri.getPort();
        username = ( (String) props.get( "username" ) );
        password = ( (String) props.get( "password" ) );

        sftpBasePath = "sftp://" + username + ":" + password + "@" + host + ":" + port;
    }

    public void init()
        throws FileSystemException
    {
        fsManager = VFS.getManager();

        opts = new FileSystemOptions();

        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking( opts, "no" );
    }

    public FileObject[] getFiles()
        throws FileSystemException
    {
        String path = sftpBasePath + remoteGetPath;

        FileObject file = fsManager.resolveFile( path, opts );

        src = file;

        FileObject[] children = file.getChildren();
        return children;
    }

    public void putFile( String filename, byte[] content )
        throws Exception
    {
        FileObject file = fsManager.resolveFile( sftpBasePath + remotePutPath + File.separator + filename, opts );

        src = file;

        OutputStream out = file.getContent().getOutputStream();

        out.write( content );

        out.close();
    }

    public boolean moveFile( String filename )
        throws FileSystemException
    {
        boolean result = false;
        FileObject srcFile = fsManager.resolveFile( sftpBasePath + remoteGetPath + File.separator + filename, opts );

        src = srcFile;
        result = moveFile( srcFile );
        return result;
    }

    public boolean moveFile( FileObject srcFile )
        throws FileSystemException
    {
        FileObject targetFile =
            fsManager.resolveFile( sftpBasePath + remoteBakPath + File.separator + srcFile.getName().getBaseName(),
                opts );
        return moveFile( srcFile, targetFile );
    }

    private boolean moveFile( FileObject srcFile, FileObject targetFile )
    {
        boolean result = false;

        if ( srcFile.canRenameTo( targetFile ) )
        {
            try
            {
                srcFile.moveTo( targetFile );
                result = true;
            }
            catch ( FileSystemException e )
            {
                e.printStackTrace();
                result = false;
            }
            return result;
        }
        return result;
    }

    public String[] getFileNames()
    {
        return null;
    }

    public InputStream processOneFile( String filename )
        throws FileSystemException
    {
        FileObject file = fsManager.resolveFile( sftpBasePath + remoteBakPath + File.separator + filename, opts );
        InputStream in;
        if ( localBak.equals( "true" ) )
        {
            in = backupAndProcessOneFile( file, "file://" + localBakPath + File.separator + filename );
        }
        else
        {
            in = processOneFileByStream( file );
        }

        return in;
    }

    public InputStream processOneFile( FileObject file )
        throws FileSystemException
    {
        InputStream in = null;
        if ( localBak.equals( "true" ) )
        {
            in =
                backupAndProcessOneFile( file, "file://" + localBakPath + File.separator + file.getName().getBaseName() );
        }
        else
        {
            in = processOneFileByStream( file );
        }

        return in;
    }

    public byte[] getInputByte( InputStream in )
        throws IOException
    {
        byte[] result = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int len = 0;
        while ( ( len = in.read( temp ) ) != -1 )
        {
            output.write( temp, 0, len );
            len = 0;
        }
        in.close();
        result = output.toByteArray();
        output.close();
        return result;
    }

    private InputStream backupAndProcessOneFile( FileObject srcFile, String localbakFilePath )
        throws FileSystemException
    {
        InputStream in = null;
        LocalFile localFile = (LocalFile) fsManager.resolveFile( localbakFilePath );
        localFile.copyFrom( srcFile, new AllFileSelector() );
        in = localFile.getContent().getInputStream();
        return in;
    }

    private InputStream processOneFileByStream( FileObject file )
        throws FileSystemException
    {
        InputStream in = null;
        in = file.getContent().getInputStream();
        return in;
    }

    public void release()
        throws Exception
    {
        FileSystem fs = null;
        if ( src != null )
        {
            src.close();
            fs = src.getFileSystem();
        }
        fsManager.closeFileSystem( fs );
    }

    public static void main( String[] args )
        throws Exception
    {
        SesameSFTPClient sftpClient = new SesameSFTPClient();
        try
        {
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
                in = sftpClient.processOneFile( filename );

                int len = in.available();
                byte[] temp = new byte[len];
                in.read( temp );
                in.close();
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        sftpClient.release();
    }
}