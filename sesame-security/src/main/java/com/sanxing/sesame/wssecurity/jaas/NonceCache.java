package com.sanxing.sesame.wssecurity.jaas;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonceCache
    extends LinkedHashMap
{
    private long nonceTTL;

    private static final Logger logger = LoggerFactory.getLogger( NonceCache.class );

    public NonceCache( long nonceTTL )
    {
        this.nonceTTL = nonceTTL;
    }

    protected boolean removeEldestEntry( Map.Entry eldest )
    {
        long eldestNonceInsertionTime = ( (Long) eldest.getValue() ).longValue();
        long currentTime = System.currentTimeMillis();
        logger.debug( "Removing nonce added at " + eldestNonceInsertionTime + " from the cache at " + currentTime );

        if ( currentTime - eldestNonceInsertionTime > this.nonceTTL * 1000L )
        {
            return true;
        }
        return false;
    }
}