package com.sanxing.sesame.jdbc.tools;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CodeGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger( DAOGenerator.class );

    public abstract Template getTemplate();

    public void generate( Class clazz )
    {
        Properties p = new Properties();
        p.setProperty( "resource.loader", "class" );
        p.setProperty( "class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        p.setProperty( "input.encoding", "UTF-8" );
        p.setProperty( "output.encoding", "UTF-8" );
        try
        {
            Velocity.init( p );
            Template template = getTemplate();
            VelocityContext context = new VelocityContext();
            TableBeanMETA meta = new TableBeanParser().parse( clazz );
            context.put( "table_name", meta.getTableName() );
            context.put( "class_name", meta.getClazzName() );
            for ( ColumnFieldMETA field : meta.getFields() )
            {
                if ( field.isPk() )
                {
                    context.put( "primary_key", field );
                }
            }
            context.put( "fields", meta.getFields() );
            Writer fw = new StringWriter();
            template.merge( context, fw );
            fw.flush();
            fw.close();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }
}