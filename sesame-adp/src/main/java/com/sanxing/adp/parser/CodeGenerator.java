package com.sanxing.adp.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.util.XJUtil;

public class CodeGenerator
{
    private final Properties p;

    private static Logger LOG = LoggerFactory.getLogger( CodeGenerator.class );

    private SchemaHolder schemaHolder;

    private final List<PortTypeInfo> interfaces = new ArrayList();

    private String targetPackageName;

    private String targetFolder;

    public CodeGenerator()
    {
        p = new Properties();
        p.setProperty( "resource.loader", "class" );
        p.setProperty( "class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        p.setProperty( "input.encoding", "UTF-8" );
        p.setProperty( "output.encoding", "UTF-8" );
        try
        {
            Velocity.init( p );
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
        }
    }

    public void setTargetFolder( String targetFolder )
    {
        this.targetFolder = targetFolder;
    }

    public void addPortType( PortTypeInfo portType )
    {
        interfaces.add( portType );
    }

    public void addSchemaHolder( SchemaHolder schemaHolder )
    {
        this.schemaHolder = schemaHolder;
    }

    public List<PortTypeInfo> getInterfaces()
    {
        return interfaces;
    }

    public void setTargetPackageName( String targetPackageName )
    {
        this.targetPackageName = targetPackageName;
    }

    public String getTargetPackageName()
    {
        return targetPackageName;
    }

    public void generate()
        throws Exception
    {
        for ( PortTypeInfo portType : interfaces )
        {
            LOG.trace( "generate code for portype " + portType );
            generateInterface( portType );
        }
        generateJAXBBean( targetPackageName );
    }

    private void generateInterface( PortTypeInfo portTypeInfo )
        throws Exception
    {
        if ( ( targetPackageName == null ) || ( targetPackageName.trim().equals( "" ) ) )
        {
            targetPackageName = XJUtil.ns2package( portTypeInfo.getName().getNamespaceURI() );
        }
        generateExceptionClass( portTypeInfo );
        generateByTemplate( portTypeInfo, "com/sanxing/adp/compiler/interface.vm", "" );
    }

    public void generateByTemplate( PortTypeInfo portTypeInfo, String templateFile, String classNameSuffix )
    {
        String clazzName = StringUtils.capitalize( portTypeInfo.getName().getLocalPart() ) + classNameSuffix;
        String date = Calendar.getInstance().getTime().toString();
        try
        {
            Template template = Velocity.getTemplate( templateFile, "UTF-8" );
            VelocityContext context = new VelocityContext();
            List<OperationInfo> infoList = portTypeInfo.getOperations();
            for ( OperationInfo info : infoList )
            {
                for ( PartInfo param : info.getParams() )
                {
                    param.setJavaType( standardizeJavaType( param.getJavaType() ) );
                }
                for ( PartInfo result : info.getResults() )
                {
                    result.setJavaType( standardizeJavaType( result.getJavaType() ) );
                }
            }
            context.put( "namcepace", portTypeInfo.getDefinationName().getNamespaceURI() );
            context.put( "portTypeName", portTypeInfo.getDefinationName().getLocalPart() );
            context.put( "operations", portTypeInfo.getOperations() );
            context.put( "date", date );
            context.put( "className", clazzName );
            context.put( "packageName", targetPackageName );

            FileWriter fw = getFileWriter( clazzName, targetPackageName );
            template.merge( context, fw );
            fw.flush();
            fw.close();
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
        }
    }

    private void generateJAXBBean( String packageName )
        throws Exception
    {
        JAXBCompiler compiler = new JAXBCompiler( schemaHolder );
        compiler.jaxbCompile( targetFolder, packageName );
    }

    private void generateExceptionClass( PortTypeInfo portTypeInfo )
        throws Exception
    {
        String packageName = targetPackageName;
        for ( OperationInfo operation : portTypeInfo.getOperations() )
        {
            for ( FaultInfo fault : operation.getFaults() )
            {
                Template template = Velocity.getTemplate( "com/sanxing/adp/compiler/fault.vm", "UTF-8" );
                VelocityContext context = new VelocityContext();
                fault.setName( fault.getName() + "Exception" );
                String ExcepName = StringUtils.capitalize( fault.getName() );
                context.put( "className", ExcepName );
                context.put( "packageName", packageName );
                Map parts = fault.getParts();
                Iterator fieldNameIter = parts.keySet().iterator();
                List fields = new LinkedList();
                while ( fieldNameIter.hasNext() )
                {
                    String fieldName = (String) fieldNameIter.next();
                    PartInfo part = (PartInfo) parts.get( fieldName );
                    String javaType = part.getJavaType();
                    Field field = new Field();
                    field.setFieldName( fieldName );
                    field.setJavaType( javaType );
                    fields.add( field );
                }
                context.put( "fields", fields );

                FileWriter fw = getFileWriter( ExcepName, packageName );
                template.merge( context, fw );
                fw.flush();
                fw.close();
            }
        }
    }

    String standardizeName( String name )
    {
        String[] ss = name.split( "_" );
        StringBuffer sb = new StringBuffer();
        for ( int index = 0; index < ss.length; ++index )
        {
            sb.append( StringUtils.capitalize( ss[index] ) );
        }
        return sb.toString();
    }

    String standardizeJavaType( String name )
    {
        String[] ss = name.split( "\\." );
        StringBuffer sb = new StringBuffer();
        for ( int index = 0; index < ss.length; ++index )
        {
            if ( index == ss.length - 1 )
            {
                sb.append( standardizeName( ss[index] ) );
            }
        }
        return sb.toString();
    }

    private FileWriter getFileWriter( String clazzName, String packageName )
        throws IOException
    {
        String packagePath = packageName.replace( ".", File.separator );
        String path = targetFolder;

        String ifaceFilePath = path + File.separator + packagePath;

        File genSrcPath = new File( ifaceFilePath );
        if ( !( genSrcPath.exists() ) )
        {
            genSrcPath.mkdirs();
        }

        FileWriter fw = new FileWriter( ifaceFilePath + File.separator + clazzName + ".java" );
        return fw;
    }
}