package com.sanxing.sesame.core.keymanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sanxing.sesame.pwd.PasswordTool;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomDriver;

public class KeyStoreManager
{
    private Map<String, KeyStoreInfo> stores = null;

    private static KeyStoreManager _instance;

    public static synchronized KeyStoreManager getInstance()
    {
        if ( _instance == null )
        {
            _instance = new KeyStoreManager();
        }
        return _instance;
    }

    private KeyStoreManager()
    {
        String serverDir = System.getProperty( "SESAME_HOME" );
        XStream xstream = new XStream( new JDomDriver() );
        File secFolder = new File( serverDir + File.separator + "security" );
        if ( !( secFolder.exists() ) )
        {
            secFolder.mkdirs();
        }
        File storeFiles = new File( secFolder, "keystore.config" );
        if ( storeFiles.exists() )
        {
            try
            {
                stores = ( (Map) xstream.fromXML( new FileInputStream( storeFiles ) ) );
            }
            catch ( FileNotFoundException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }
        else
        {
            try
            {
                stores = new HashMap();
                storeFiles.createNewFile();
                FileOutputStream fout = new FileOutputStream( storeFiles );
                xstream.toXML( stores, fout );
                fout.flush();
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }
    }

    public void addKeyStore( KeyStoreInfo info )
    {
        try
        {
            info.setStorePass( PasswordTool.encrypt( info.getStorePass() ) );
            stores.put( info.getName(), info );
            persistence();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public void delKeyStore( String name )
    {
        try
        {
            stores.remove( name );

            persistence();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private void persistence()
        throws FileNotFoundException
    {
        String serverDir = System.getProperty( "SESAME_HOME" );
        OutputStream out =
            new FileOutputStream( serverDir + File.separator + "security" + File.separator + "keystore.config" );
        XStream xstream = new XStream( new JDomDriver() );
        xstream.toXML( stores, out );
    }

    public KeyStoreInfo getKeyStore( String name )
    {
        try
        {
            KeyStoreInfo info = stores.get( name );
            KeyStoreInfo temp = new KeyStoreInfo();
            temp.setName( info.getName() );
            temp.setKeystorePath( info.getKeystorePath() );
            temp.setStorePass( PasswordTool.decrypt( info.getStorePass() ) );
            temp.setDescription( info.getDescription() );

            return temp;
        }
        catch ( Exception e )
        {
        }
        return null;
    }

    public List<KeyStoreInfo> getAllKeyStore()
    {
        List list = new ArrayList();
        try
        {
            Iterator itr = stores.entrySet().iterator();
            while ( itr.hasNext() )
            {
                Map.Entry entry = (Map.Entry) itr.next();
                KeyStoreInfo info = getKeyStore( (String) entry.getKey() );
                list.add( info );
            }
        }
        catch ( Exception e )
        {
            return null;
        }
        return list;
    }
}