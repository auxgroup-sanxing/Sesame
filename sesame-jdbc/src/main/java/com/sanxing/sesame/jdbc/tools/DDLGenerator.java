package com.sanxing.sesame.jdbc.tools;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

public class DDLGenerator
    extends CodeGenerator
{
    @Override
    public Template getTemplate()
    {
        try
        {
            Template template = Velocity.getTemplate( "com/sanxing/sesame.jdbc/tools/DDL.vm", "UTF-8" );
            return template;
        }
        catch ( Exception e )
        {
        }
        return null;
    }
}