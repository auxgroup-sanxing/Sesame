package com.sanxing.sesame.dispatch.cluster;

import java.util.concurrent.atomic.AtomicInteger;

import com.sanxing.sesame.servicedesc.InternalEndpoint;

public class RoundRobbinCEChooser
    implements ClusterEndpointChooser
{
    private final InternalEndpoint localPoint;

    private final AtomicInteger next = new AtomicInteger( 0 );

    private final AtomicInteger limit = new AtomicInteger( 0 );

    public RoundRobbinCEChooser( InternalEndpoint localPoint )
    {
        this.localPoint = localPoint;

        check( localPoint );
    }

    private void check( InternalEndpoint localPoint )
    {
        if ( localPoint.getComponentNameSpace() == null )
        {
            if ( localPoint.getRemoteEndpoints().length != limit.get() )
            {
                limit.set( localPoint.getRemoteEndpoints().length );
            }
        }
        else if ( localPoint.getRemoteEndpoints().length + 1 != limit.get() )
        {
            limit.set( localPoint.getRemoteEndpoints().length + 1 );
        }
    }

    private void reset()
    {
        next.set( 0 );
    }

    @Override
    public InternalEndpoint choose( InternalEndpoint localPoint, InternalEndpoint[] remotes )
    {
        check( localPoint );
        int cursor = next.getAndIncrement();
        if ( cursor < limit.get() )
        {
            if ( this.localPoint.getComponentNameSpace() == null )
            {
                return remotes[cursor];
            }
            if ( cursor == 0 )
            {
                return localPoint;
            }
            return remotes[( cursor - 1 )];
        }

        reset();
        return choose( localPoint, remotes );
    }
}