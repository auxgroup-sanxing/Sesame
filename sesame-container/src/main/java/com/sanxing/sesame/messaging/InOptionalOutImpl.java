package com.sanxing.sesame.messaging;

import java.io.IOException;
import java.io.ObjectInput;

import javax.jbi.messaging.InOptionalOut;

public class InOptionalOutImpl
    extends MessageExchangeImpl
    implements InOptionalOut
{
    private static final long serialVersionUID = -3145649037372074912L;

    private static final int[][] STATES_CONSUMER = { { 593, 1, -1, -1, -1 }, { 0, 2, 3, 4, 4 }, { 980, -1, 5, 6, 6 },
        { 912, -1, -1, 6, 6 }, { 512, -1, -1, -1, -1 }, { 0, -1, -1, 7, 7 }, { 0, -1, -1, -1, -1 },
        { 512, -1, -1, -1, -1 } };

    private static final int[][] STATES_PROVIDER = { { 8, 1, -1, -1 }, { 990, 2, 3, 4, 4 }, { 8, -1, 5, 6, 6 },
        { 8, -1, -1, 6, 6 }, { 8, -1, -1, -1, -1 }, { 920, -1, -1, 7, 7 }, { 520, -1, -1, -1, -1 },
        { 8, -1, -1, -1, -1 } };

    public InOptionalOutImpl()
    {
    }

    public InOptionalOutImpl( String exchangeId )
    {
        super( exchangeId, MessageExchangeSupport.IN_OPTIONAL_OUT, STATES_CONSUMER );
        mirror = new InOptionalOutImpl( this );
    }

    public InOptionalOutImpl( ExchangePacket packet )
    {
        super( packet, STATES_CONSUMER );
        mirror = new InOptionalOutImpl( this );
    }

    protected InOptionalOutImpl( InOptionalOutImpl mep )
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
        mirror = new InOptionalOutImpl();
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