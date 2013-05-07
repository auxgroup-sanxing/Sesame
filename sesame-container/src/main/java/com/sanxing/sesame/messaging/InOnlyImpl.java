package com.sanxing.sesame.messaging;

import java.io.IOException;
import java.io.ObjectInput;

import javax.jbi.messaging.InOnly;

public class InOnlyImpl
    extends MessageExchangeImpl
    implements InOnly
{
    private static final long serialVersionUID = -4851111881482457905L;

    private static final int[][] STATES_CONSUMER = { { 593, 1, -1, -1, -1 }, { 0, -1, -1, 2, 2 },
        { 512, -1, -1, -1, -1 } };

    private static final int[][] STATES_PROVIDER =
        { { 8, 1, -1, -1, -1 }, { 920, -1, -1, 2, 2 }, { 8, -1, -1, -1, -1 } };

    public InOnlyImpl()
    {
    }

    public InOnlyImpl( String exchangeId )
    {
        super( exchangeId, MessageExchangeSupport.IN_ONLY, STATES_CONSUMER );
        mirror = new InOnlyImpl( this );
    }

    public InOnlyImpl( ExchangePacket packet )
    {
        super( packet, STATES_CONSUMER );
        mirror = new InOnlyImpl( this );
    }

    protected InOnlyImpl( InOnlyImpl mep )
    {
        super( mep.packet, STATES_PROVIDER );
        mirror = mep;
    }

    @Override
    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        packet = new ExchangePacket();
        packet.readExternal( in );
        if ( packet.in != null )
        {
            packet.in.exchange = this;
        }
        if ( packet.out != null )
        {
            packet.out.exchange = this;
        }
        if ( packet.fault != null )
        {
            packet.fault.exchange = this;
        }
        state = in.read();
        mirror = new InOnlyImpl();
        mirror.mirror = this;
        mirror.packet = packet;
        mirror.state = in.read();
        boolean provider = in.readBoolean();
        if ( provider )
        {
            states = STATES_PROVIDER;
            mirror.states = STATES_CONSUMER;
        }
        else
        {
            states = STATES_CONSUMER;
            mirror.states = STATES_PROVIDER;
        }
    }
}