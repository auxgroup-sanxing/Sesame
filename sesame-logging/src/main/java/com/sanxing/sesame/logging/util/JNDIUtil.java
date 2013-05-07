package com.sanxing.sesame.logging.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sanxing.sesame.util.ServerDetector;

public class JNDIUtil
{
    private static InitialContext ic;

    public static InitialContext getInitialContext()
    {
        if ( ic == null )
        {
            try
            {
                if ( ( !( ServerDetector.isInContainer().booleanValue() ) )
                    && ( ( ( System.getProperty( "java.naming.factory.initial" ) == null ) || ( System.getProperty( "org.osjava.sj.jndi.shared" ) == null ) ) ) )
                {
                    System.setProperty( "java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory" );
                    System.setProperty( "org.osjava.sj.jndi.shared", "true" );
                }

                ic = new InitialContext();
            }
            catch ( NamingException e )
            {
                e.printStackTrace();
                return null;
            }
        }
        return ic;
    }
}