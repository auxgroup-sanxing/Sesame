package com.sanxing.sesame.jdbc.tools;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOGenerator
    extends CodeGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger( DAOGenerator.class );

    @Override
    public Template getTemplate()
    {
        try
        {
            Template template = Velocity.getTemplate( "com/sanxing/sesame.jdbc/tools/DAO.vm", "UTF-8" );
            return template;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        return null;
    }
}