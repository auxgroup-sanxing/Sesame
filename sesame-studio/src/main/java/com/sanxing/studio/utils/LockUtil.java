package com.sanxing.studio.utils;

import java.io.File;
import java.util.Map;

import org.json.JSONObject;

import com.sanxing.studio.Authentication;
import com.sanxing.studio.Configuration;
import com.sanxing.studio.team.SCM;
import com.sanxing.studio.team.ThreeWaySynchronizer;

public class LockUtil
{
    public static String isOperaLocked( String schema )
        throws Exception
    {
        String isLocked = "false";
        File xsdFile = new File( schema );
        if ( !( xsdFile.exists() ) )
        {
            xsdFile = Configuration.getWorkspaceFile( schema );
        }
        if ( !( xsdFile.exists() ) )
        {
            throw new Exception( "操作异常," + xsdFile.getName() + "不存在！" );
        }
        File unitFolder = xsdFile.getParentFile();
        File wsdlFile = new File( unitFolder, "unit.wsdl" );

        JSONObject user = Authentication.getCurrentUser();
        String userName = user.optString( "userid" );

        ThreeWaySynchronizer sync = SCM.getSynchronizer( unitFolder.getParentFile().getParentFile() );

        if ( ( sync != null ) && ( sync.isVersioned( wsdlFile ) ) && ( sync.isVersioned( xsdFile ) ) )
        {
            Map props = sync.info( wsdlFile );
            String lock = (String) props.get( "lock" );

            if ( lock == null )
            {
                props = sync.info( xsdFile );
                lock = (String) props.get( "lock" );
                if ( ( lock != null ) && ( userName.equals( props.get( "lock.owner" ) ) ) )
                {
                    isLocked = "true";
                }
            }
            else if ( userName.equals( props.get( "lock.owner" ) ) )
            {
                isLocked = "true";
            }
        }
        else
        {
            isLocked = "disconnect";
        }

        return isLocked;
    }

    public static void deleteFromSvn( ThreeWaySynchronizer synchronizer, File file )
        throws Exception
    {
        if ( ( synchronizer != null ) && ( synchronizer.isVersioned( file ) ) )
        {
            synchronizer.delete( file );
        }
    }

    public static void renameFromSvn( ThreeWaySynchronizer synchronizer, File file, File newFile )
        throws Exception
    {
        if ( ( synchronizer != null ) && ( synchronizer.isVersioned( file ) ) )
        {
            synchronizer.move( file, newFile );
        }
    }
}