package com.sanxing.sesame.engine.action.flow.exceptions;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.action.callout.CalloutException;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.exceptions.KeyedErr;

public class TryCatchAction
    extends AbstractAction
    implements Constant
{
    private Element actionEl;

    private static final Logger LOGGER = LoggerFactory.getLogger( TryCatchAction.class );

    private static Pattern SPLIT_REGEX = Pattern.compile( "," );

    @Override
    public void doinit( Element config )
    {
        actionEl = config;
    }

    @Override
    public void dowork( DataContext dataCtx )
        throws AppException
    {
        Iterator tryActions = actionEl.getChild( "try" ).getChildren().iterator();
        try
        {
            ActionUtil.bachInvoke( dataCtx, tryActions );
        }
        catch ( Exception e )
        {
            String exceptionKey = getExceptionKey( e );
            String exceptionMsg = getExceptionMsg( e );
            if ( e instanceof CalloutException )
            {
                exceptionKey = ( (CalloutException) e ).getKey();
            }
            if ( e instanceof KeyedErr )
            {
                KeyedErr err = (KeyedErr) e;
                exceptionKey = err.getErrKey();
                if ( ( e instanceof ActionException ) && ( e.getCause() instanceof KeyedErr ) )
                {
                    exceptionKey = ( (KeyedErr) e.getCause() ).getErrKey();
                    exceptionMsg = e.getCause().getMessage();
                }

            }

            boolean catched = false;
            List catches = actionEl.getChildren( "catch" );
            for ( Iterator iter = catches.iterator(); iter.hasNext(); )
            {
                Element catchEl = (Element) iter.next();
                String strKeys = catchEl.getAttributeValue( "exception-key", "" );
                String[] exceptionKeys = ( strKeys.length() > 0 ) ? SPLIT_REGEX.split( strKeys ) : new String[0];
                for ( int i = 0; i < exceptionKeys.length; ++i )
                {
                    String key = exceptionKeys[i];
                    if ( key.startsWith( "$" ) )
                    {
                        exceptionKeys[i] = dataCtx.getVariable( key.substring( 1 ) ).toString();
                    }
                }
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( "Catchable exceptions: [" + strKeys + "]" );
                }
                boolean catchable = Catcher.isCatchable( exceptionKey, exceptionKeys );
                if ( catchable )
                {
                    catched = true;
                    if ( LOGGER.isDebugEnabled() )
                    {
                        LOGGER.debug( "Catched exception ", e );
                    }

                    Variable statusVar = new Variable( exceptionKey, 7 );
                    dataCtx.addVariable( "faultcode", statusVar );
                    Variable descVar = new Variable( exceptionMsg, 7 );
                    dataCtx.addVariable( "faultstring", descVar );
                    dataCtx.getExecutionContext().put( "process.faultcode", exceptionKey );
                    dataCtx.getExecutionContext().put( "process.faultstring", exceptionMsg );

                    ActionUtil.bachInvoke( dataCtx, catchEl.getChildren().iterator() );

                    String rethrow = catchEl.getAttributeValue( "throw", "false" );
                    if ( !( rethrow.equals( "true" ) ) )
                    {
                        break;
                    }
                    if ( e instanceof AppException )
                    {
                        throw ( (AppException) e );
                    }
                    throw ( (RuntimeException) e );
                }

            }

            if ( !( catched ) )
            {
                if ( e instanceof AppException )
                {
                    throw ( (AppException) e );
                }

                throw ( (RuntimeException) e );
            }
        }
    }

    private String getExceptionKey( Exception exception )
    {
        String message = exception.getMessage();
        if ( message == null )
        {
            return "";
        }

        int p = message.indexOf( 124, 0 );
        return ( ( p > -1 ) ? message.substring( 0, p ) : "" );
    }

    private String getExceptionMsg( Exception exception )
    {
        String message = exception.getMessage();
        if ( message == null )
        {
            return null;
        }

        int p = message.indexOf( 124, 0 );
        return ( ( p > -1 ) ? message.substring( p + 1 ) : message );
    }
}