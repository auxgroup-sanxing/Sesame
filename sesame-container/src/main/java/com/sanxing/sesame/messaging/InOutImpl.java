package com.sanxing.sesame.messaging;

import java.io.IOException;
import java.io.ObjectInput;

import javax.jbi.messaging.InOut;

public class InOutImpl
    extends MessageExchangeImpl
    implements InOut
{
    private static final long serialVersionUID = -1639492357707831113L;

    private static final int[][] STATES_CONSUMER = { { 593, 1, -1, -1, -1 }, { 0, 2, 2, 3, 3 }, { 912, -1, -1, 4, 4 },
        { 512, -1, -1, -1, -1 }, { 0, -1, -1, -1, -1 } };

    private static final int[][] STATES_PROVIDER = { { 8, 1, -1, -1 }, { 862, 2, 2, 3, -1 }, { 8, -1, -1, 4, 4 },
        { 8, -1, -1, -1, -1 }, { 520, -1, -1, -1, -1 } };

    public InOutImpl()
    {
    }

    public InOutImpl( String exchangeId )
    {
        super( exchangeId, MessageExchangeSupport.IN_OUT, STATES_CONSUMER );
        mirror = new InOutImpl( this );
    }

    public InOutImpl( ExchangePacket packet )
    {
        super( packet, STATES_CONSUMER );
        mirror = new InOutImpl( this );
    }

    protected InOutImpl( InOutImpl mep )
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
        mirror = new InOutImpl();
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