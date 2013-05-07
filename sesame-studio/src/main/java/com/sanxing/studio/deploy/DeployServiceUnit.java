package com.sanxing.studio.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.eclipse.ADPServiceProjectBuilder;
import com.sanxing.sesame.container.ServiceAssemblyEnvironment;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.deployment.Descriptor;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.mbean.ServiceAssemblyLifeCycle;
import com.sanxing.studio.utils.JdomUtil;

public class DeployServiceUnit
{
    private final String servername;

    private final MBeanServer mserver;

    private final Object[] paramsnull;

    private final String[] signaturenull;

    private Boolean assemblyState;

    private final Logger log;

    public DeployServiceUnit()
    {
        servername = Platform.getEnv().getServerName();
        mserver = Platform.getLocalMBeanServer();
        paramsnull = new Object[0];
        signaturenull = new String[0];
        assemblyState = Boolean.valueOf( true );
        log = LoggerFactory.getLogger( DeployServiceUnit.class );
    }

    private void copySchema( String srcSchema, String desSchema )
        throws Exception
    {
        try
        {
            Map sschemamap = new HashMap();
            FileInputStream fileIS = null;
            FileOutputStream fileOS = null;
            File sschema = new File( srcSchema );
            File[] sschemalist = sschema.listFiles();
            if ( sschemalist == null )
            {
                return;
            }
            for ( int i = 0; i < sschemalist.length; ++i )
            {
                File schemafile = sschemalist[i];
                if ( schemafile.isFile() )
                {
                    sschemamap.put( schemafile.getName(), schemafile );
                }
            }
            File dschema = new File( desSchema );
            if ( !( dschema.exists() ) )
            {
                dschema.mkdirs();
            }
            File[] dschemalist = dschema.listFiles();
            if ( dschemalist == null )
            {
                DirectoryCopy.copyDirectory( srcSchema, desSchema );
            }
            else
            {
                for ( int j = 0; j < dschemalist.length; ++j )
                {
                    File schemafile = dschemalist[j];
                    if ( schemafile.isDirectory() )
                    {
                        continue;
                    }
                    File sschemafile = (File) sschemamap.get( schemafile.getName() );
                    if ( ( ( ( sschemafile == null ) || ( schemafile.lastModified() <= sschemafile.lastModified() ) ) )
                        && ( sschemafile != null ) )
                    {
                        continue;
                    }
                    try
                    {
                        fileIS = new FileInputStream( schemafile );
                        fileOS = new FileOutputStream( sschemafile );
                        byte[] bt = new byte[4096];
                        int readNum = 0;
                        while ( ( readNum = fileIS.read( bt ) ) != -1 )
                        {
                            fileOS.write( bt, 0, readNum );
                        }
                        fileIS.close();
                        fileOS.close();
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                        try
                        {
                            fileIS.close();
                            fileOS.close();
                        }
                        catch ( IOException e1 )
                        {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public Boolean checkComponentStatus( String componentName )
        throws Exception
    {
        Boolean componentStatus = Boolean.valueOf( false );
        try
        {
            ObjectName objName =
                ObjectName.getInstance( ":ServerName=" + servername + ",Type=Component,Name=" + componentName
                    + ",SubType=LifeCycle" );

            Set set = mserver.queryMBeans( objName, null );
            String state = "";
            for ( Iterator it = set.iterator(); it.hasNext(); )
            {
                ObjectInstance obj = (ObjectInstance) it.next();
                state =
                    (String) mserver.invoke( obj.getObjectName(), "getRunningStateFromStore", paramsnull, signaturenull );
            }

            if ( state.equals( "Started" ) )
            {
                componentStatus = Boolean.valueOf( true );
            }
            else
            {
                componentStatus = Boolean.valueOf( false );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return componentStatus;
    }

    public void deployeServiceUnit( String serviceassemblyname, String serviceUnitName, String componentName,
                                    String srcDirectoryPath )
        throws Exception
    {
        try
        {
            ADPServiceProjectBuilder adpProject = new ADPServiceProjectBuilder();
            adpProject.buildAll( srcDirectoryPath );

            ObjectName objName =
                ObjectName.getInstance( ":ServerName=" + servername + ",Type=ServiceUnitAdaptor,Name="
                    + serviceUnitName + ",*" );

            String suspath = getServiceAssemblyPath( serviceassemblyname, "sus" );
            String desDirectoryPath = suspath + File.separator + componentName + File.separator + serviceUnitName;

            File tempf = new File( srcDirectoryPath );

            String installpath = getServiceAssemblyPath( serviceassemblyname, "install" );
            String jbipath = installpath + File.separator + "jbi.xml";
            File jbixmlFile = new File( jbipath );
            if ( !( jbixmlFile.exists() ) )
            {
                createNewJbiFile( tempf.getParentFile().getParentFile().getAbsolutePath() + File.separator + "jbi.xml",
                    jbixmlFile );
            }

            String desSchemapath = suspath + File.separator + "schema";
            String srcSchemapath = tempf.getParentFile().getParentFile().getAbsolutePath() + File.separator + "schema";
            copySchema( srcSchemapath, desSchemapath );

            Set set = mserver.queryMBeans( objName, null );
            if ( set.size() < 1 )
            {
                if ( checkComponentStatus( componentName ).booleanValue() )
                {
                    DirectoryCopy.copyDirectory( srcDirectoryPath, desDirectoryPath );
                    addJbiFile( jbipath, serviceUnitName, componentName );
                    registerServiceAssembly( serviceassemblyname );
                }
                else
                {
                    throw new Exception( componentName + "组件未正常工作！" );
                }
            }

            for ( Iterator it = set.iterator(); it.hasNext(); )
            {
                ObjectInstance obj = (ObjectInstance) it.next();
                mserver.invoke( obj.getObjectName(), "stop", paramsnull, signaturenull );
                DirectoryCopy.copyDirectory( srcDirectoryPath, desDirectoryPath );
                mserver.invoke( obj.getObjectName(), "init", paramsnull, signaturenull );
                mserver.invoke( obj.getObjectName(), "start", paramsnull, signaturenull );
            }

            if ( !( checkServiceUnitStatus( serviceUnitName ).booleanValue() ) )
            {
                throw new Exception( "服务单元部署出错，请查看后台日志！" );
            }
        }
        catch ( Exception e )
        {
            throw e;
        }
    }

    private Boolean checkServiceUnitStatus( String serviceUnitName )
        throws Exception
    {
        Boolean serviceUnitStatus = Boolean.valueOf( false );
        Iterator it;
        try
        {
            ObjectName objName =
                ObjectName.getInstance( ":ServerName=" + servername + ",Type=ServiceUnitAdaptor,Name="
                    + serviceUnitName + ",*" );

            Set set = mserver.queryMBeans( objName, null );
            for ( it = set.iterator(); it.hasNext(); )
            {
                ObjectInstance obj = (ObjectInstance) it.next();
                String status =
                    (String) mserver.invoke( obj.getObjectName(), "getCurrentState", paramsnull, signaturenull );

                if ( status.equals( "Started" ) )
                {
                    serviceUnitStatus = Boolean.valueOf( true );
                }
            }
        }
        catch ( Exception e )
        {
            throw e;
        }
        return serviceUnitStatus;
    }

    private void createNewJbiFile( String srcjbipath, File jbixmlFile )
        throws Exception
    {
        log.debug( "the target jbifile path " + jbixmlFile.getAbsolutePath() );
        try
        {
            jbixmlFile.getParentFile().mkdirs();
            jbixmlFile.createNewFile();
            File srcJbiFile = new File( srcjbipath );
            SAXBuilder builder = new SAXBuilder();
            Document newdoc = builder.build( srcJbiFile );
            Element jbidoc =
                newdoc.getRootElement().getChild( "service-assembly", newdoc.getRootElement().getNamespace() );

            jbidoc.removeChildren( "service-unit", newdoc.getRootElement().getNamespace() );
            writeFile( newdoc, new FileOutputStream( jbixmlFile ) );
        }
        catch ( Exception e )
        {
            throw e;
        }
    }

    private String getServiceAssemblyPath( String serviceassemblyname, String directoryname )
        throws Exception
    {
        String serviceassemblypath = "";
        Iterator it;
        try
        {
            ObjectName objName =
                ObjectName.getInstance( ":ServerName=" + servername + ",Type=ServiceAssembly,Name="
                    + serviceassemblyname );

            Set set = mserver.queryMBeans( objName, null );
            if ( set.size() < 1 )
            {
                assemblyState = Boolean.valueOf( false );
                if ( directoryname.equals( "install" ) )
                {
                    serviceassemblypath =
                        System.getProperty( "SESAME_HOME" ) + File.separator + "work" + File.separator
                            + "service-assemblies" + File.separator + serviceassemblyname + File.separator
                            + "version_1" + File.separator + "install";
                }
                else
                {
                    serviceassemblypath =
                        System.getProperty( "SESAME_HOME" ) + File.separator + "work" + File.separator
                            + "service-assemblies" + File.separator + serviceassemblyname + File.separator
                            + "version_1" + File.separator + "sus";
                }
            }

            for ( it = set.iterator(); it.hasNext(); )
            {
                ObjectInstance obj = (ObjectInstance) it.next();

                ServiceAssemblyEnvironment env =
                    (ServiceAssemblyEnvironment) mserver.invoke( obj.getObjectName(), "getEnvironment", paramsnull,
                        signaturenull );

                if ( directoryname.equals( "install" ) )
                {
                    serviceassemblypath = env.getInstallDir().getAbsolutePath();
                }
                else
                {
                    serviceassemblypath = env.getSusDir().getAbsolutePath();
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
        return serviceassemblypath;
    }

    private void addJbiFile( String jbifile, String serviceUnitName, String componentName )
        throws Exception
    {
        File xmlFile = new File( jbifile );
        SAXBuilder builder = new SAXBuilder();
        try
        {
            Document newdoc = builder.build( xmlFile );
            Element su = new Element( "service-unit", newdoc.getRootElement().getNamespace() );
            Element target = new Element( "target", newdoc.getRootElement().getNamespace() );
            Element arti = new Element( "artifacts-zip", newdoc.getRootElement().getNamespace() );
            arti.addContent( serviceUnitName + ".zip" );
            Element comname = new Element( "component-name", newdoc.getRootElement().getNamespace() );
            comname.addContent( componentName );
            target.addContent( arti );
            target.addContent( comname );
            Element id = new Element( "identification", newdoc.getRootElement().getNamespace() );
            Element name = new Element( "name", newdoc.getRootElement().getNamespace() );
            name.addContent( serviceUnitName );
            Element description = new Element( "description", newdoc.getRootElement().getNamespace() );
            id.addContent( name );
            id.addContent( description );
            Element sa = newdoc.getRootElement().getChild( "service-assembly", newdoc.getRootElement().getNamespace() );

            su.addContent( id );
            su.addContent( target );
            sa.addContent( su );
            writeFile( newdoc, new FileOutputStream( jbifile ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void writeFile( Document newdoc, FileOutputStream out )
        throws Exception
    {
        try
        {
            JdomUtil.getPrettyOutputter().output( newdoc, out );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void registerServiceAssembly( String assemblyName )
        throws Exception
    {
        Object[] params;
        String[] signature;
        Object[] params1;
        String[] signature1;
        Iterator it;
        Iterator it1;
        try
        {
            ObjectName objName =
                ObjectName.getInstance( ":ServerName=" + servername + ",Type=SystemService,Name=EnvironmentContext" );

            Set set = mserver.queryMBeans( objName, null );
            for ( it = set.iterator(); it.hasNext(); )
            {
                ObjectInstance obj = (ObjectInstance) it.next();
                params1 = new Object[1];
                signature1 = new String[1];
                params1[0] = assemblyName;
                signature1[0] = String.class.getName();
                ServiceAssemblyEnvironment env =
                    (ServiceAssemblyEnvironment) mserver.invoke( obj.getObjectName(), "getServiceAssemblyEnvironment",
                        params1, signature1 );

                Descriptor root = DescriptorFactory.buildDescriptor( env.getInstallDir() );
                if ( root != null )
                {
                    ServiceAssembly sa = root.getServiceAssembly();
                    if ( ( sa != null ) && ( sa.getIdentification() != null ) )
                    {
                        params = new Object[2];
                        signature = new String[2];
                        params[0] = sa;
                        params[1] = env;
                        signature[0] = ServiceAssembly.class.getName();
                        signature[1] = ServiceAssemblyEnvironment.class.getName();
                        ObjectName objName1 =
                            ObjectName.getInstance( ":ServerName=" + servername + ",Type=SystemService,Name=Registry" );

                        Set set1 = mserver.queryMBeans( objName1, null );
                        for ( it1 = set1.iterator(); it1.hasNext(); )
                        {
                            ObjectInstance obj1 = (ObjectInstance) it1.next();
                            if ( assemblyState.booleanValue() )
                            {
                                shutDownSA( assemblyName );
                                mserver.invoke( obj1.getObjectName(), "unregisterServiceAssembly", params1, signature1 );
                            }
                            ServiceAssemblyLifeCycle neoSAL =
                                (ServiceAssemblyLifeCycle) mserver.invoke( obj1.getObjectName(),
                                    "registerServiceAssembly", params, signature );

                            neoSAL.start();
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void shutDownSA( String assemblyName )
        throws Exception
    {
        Iterator it;
        try
        {
            ObjectName objName =
                ObjectName.getInstance( ":ServerName=" + servername + ",Type=ServiceAssembly,Name=" + assemblyName );

            Set set = mserver.queryMBeans( objName, null );
            for ( it = set.iterator(); it.hasNext(); )
            {
                ObjectInstance obj = (ObjectInstance) it.next();
                mserver.invoke( obj.getObjectName(), "shutDown", paramsnull, signaturenull );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }
}