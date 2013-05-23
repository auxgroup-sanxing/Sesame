package com.sanxing.sesame.engine.action.sla;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;

import static com.sanxing.sesame.engine.ExecutionEnv.*;

public class LogAction
    extends AbstractAction
    implements Constant
{
    private String loggerName;

    private String level = "debug";

    private String varName = "body";

    private String msg = "";

    private String xPath;

    static Logger log;

    @Override
    public void doinit( Element config )
    {
        loggerName = config.getAttributeValue( "name" );
        level = config.getAttributeValue( "level", "debug" );
        varName = config.getAttributeValue( "var", "anonymous" );
        msg = config.getAttributeValue( "msg", "" ).replace( "*", "" );
        xPath = config.getChildTextTrim( "xpath", config.getNamespace() );
        if ( loggerName != null )
        {
            log = LoggerFactory.getLogger( loggerName );
        }
        else
        {
            log = LoggerFactory.getLogger( super.getClass() );
        }
    }

    @Override
    public void dowork( DataContext dataContext )
    {
        String toBeDebug = "\n-----------------------------------\n";
        try
        {
            String[] logMSG = msg.split( "[{*}]" );
            for ( int i = 0; i < logMSG.length; ++i )
            {
                if ( ( msg.indexOf( "{" + logMSG[i] + "}", 0 ) > 0 ) || ( msg.startsWith( "{" + logMSG[i] + "}" ) ) )
                {
                    toBeDebug = toBeDebug + getVariable( dataContext, "request", logMSG[i] );
                }
                else
                {
                    toBeDebug = toBeDebug + logMSG[i].replace( "", "*" );
                }
            }
            toBeDebug = toBeDebug + "\n" + "-----------------------------------";

            Long serial = (Long) dataContext.getExecutionContext().get( SERIAL_NUMBER );

            if ( serial != null )
            {
                toBeDebug = "{serial:" + serial + "}" + toBeDebug;
            }

            Log logger = LogFactory.getLog( "sesame.application" );
            if ( logger != null )
            {
                if ( level.equals( "debug" ) )
                {
                    logger.debug( toBeDebug );
                }
                else if ( level.equalsIgnoreCase( "info" ) )
                {
                    logger.info( toBeDebug );
                }
                else if ( level.equalsIgnoreCase( "warn" ) )
                {
                    logger.warn( toBeDebug );
                }
                else if ( level.equalsIgnoreCase( "error" ) )
                {
                    logger.error( toBeDebug );
                }
                else if ( level.equalsIgnoreCase( "fatal" ) )
                {
                    logger.fatal( toBeDebug );
                }
                return;
            }

            if ( level.equals( "debug" ) )
            {
                log.debug( toBeDebug );
                return;
            }
            if ( level.equalsIgnoreCase( "warn" ) )
            {
                log.warn( toBeDebug );
                return;
            }
            if ( level.equalsIgnoreCase( "error" ) )
            {
                log.error( toBeDebug );
                return;
            }
            if ( level.equalsIgnoreCase( "fatal" ) )
            {
                log.error( toBeDebug );
                return;
            }
            if ( level.equalsIgnoreCase( "info" ) )
            {
                log.info( toBeDebug );
            }
        }
        catch ( ActionException e )
        {
            log.error( e.getMessage() );

            throw e;
        }
    }

    public static void main( String[] args )
    {
        String log1 = "asdas.{aa}*0*{s.s}a'a.'a'a";
        String log = log1.replace( "*", "" );

        String[] xpath = log.split( "[{*}]" );
        for ( int i = 0; i < xpath.length; ++i )
        {
            if ( ( log.indexOf( "{" + xpath[i] + "}", 0 ) > 0 ) || ( log.startsWith( "{" + xpath[i] + "}" ) ) )
            {
                System.out.println( xpath[i] + " is var ...." );
            }
            else
            {
                System.out.println( xpath[i].replace( "", "*" ) + " isn't vat!!!" );
            }
        }
    }
}