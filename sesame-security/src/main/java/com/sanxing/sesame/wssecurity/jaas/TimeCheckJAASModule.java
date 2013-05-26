package com.sanxing.sesame.wssecurity.jaas;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeCheckJAASModule
    implements LoginModule
{
    public static final String TIMESTAMP_MANDATORY_OPTION = "timestampMandatory";

    private boolean timestampMandatory;

    public static final String TOKEN_EXPIRY_DURATION_OPTION = "timeoutInSeconds";

    public static final long DEFAULT_TOKEN_EXPIRY_DURATION = 300L;

    private Duration tokenExpiryDuration;

    private DatatypeFactory xmlDatatypeFactory;

    protected CallbackHandler callbackHandler;

    private static final Logger logger = LoggerFactory.getLogger( TimeCheckJAASModule.class );

    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options )
    {
        logger.debug( "intializing" );

        this.callbackHandler = callbackHandler;

        this.timestampMandatory = Boolean.parseBoolean( (String) options.get( "timestampMandatory" ) );
        logger.debug( "timestamp is: " + ( this.timestampMandatory ? "mandatory" : "optional" ) );
        try
        {
            this.xmlDatatypeFactory = DatatypeFactory.newInstance();
        }
        catch ( DatatypeConfigurationException e )
        {
            logger.error( e.getMessage(), e );
            throw new RuntimeException( e );
        }

        String expiryDurationAsString = (String) options.get( "timeoutInSeconds" );
        if ( expiryDurationAsString != null )
        {
            logger.debug( "setting expiry duration (in secs.) to: " + expiryDurationAsString );
            this.tokenExpiryDuration =
                this.xmlDatatypeFactory.newDuration( Long.parseLong( expiryDurationAsString ) * 1000L );
        }
        else
        {
            logger.debug( "setting expiry duration (in secs.) to: 300" );

            this.tokenExpiryDuration = this.xmlDatatypeFactory.newDuration( 300L );
        }
    }

    public boolean login()
        throws LoginException
    {
        logger.debug( "Rcvd. a new login request" );
        if ( this.callbackHandler == null )
        {
            throw new LoginException( "Error: No CallbackHandler to get time" );
        }

        Callback[] callbacks = { new TimestampCallback() };
        try
        {
            this.callbackHandler.handle( callbacks );
        }
        catch ( IOException ioe )
        {
            logger.error( ioe.getMessage(), ioe );
            throw new LoginException( "Internal error in authentication" );
        }
        catch ( UnsupportedCallbackException uce )
        {
            logger.error( "Error: " + uce.getCallback().getClass().getName() + " not supported by authenticating app",
                uce );
            throw new LoginException( "Internal error in authentication" );
        }

        String timestamp = ( (TimestampCallback) callbacks[0] ).getTimestampAsString();
        if ( timestamp == null )
        {
            if ( this.timestampMandatory )
            {
                logger.warn( "Timestamp missing" );
                throw new LoginException( "Timestamp missing" );
            }
            logger.debug( "no timestamp in request" );
            return false;
        }

        logger.debug( "Checking if time expired. Timestamp is: " + timestamp );

        XMLGregorianCalendar tokenExpiryTime = this.xmlDatatypeFactory.newXMLGregorianCalendar( timestamp );
        tokenExpiryTime.add( this.tokenExpiryDuration );

        XMLGregorianCalendar now =
            this.xmlDatatypeFactory.newXMLGregorianCalendar( new GregorianCalendar( TimeZone.getTimeZone( "UTC" ) ) );
        logger.debug( "Current time: " + now.toXMLFormat() );
        if ( now.compare( tokenExpiryTime ) == 1 )
        {
            logger.warn( "Expired request: Replay attack?" );
            throw new LoginException( "Time expired" );
        }

        return true;
    }

    public boolean commit()
        throws LoginException
    {
        return true;
    }

    public boolean abort()
        throws LoginException
    {
        return true;
    }

    public boolean logout()
        throws LoginException
    {
        return true;
    }
}