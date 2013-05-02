package com.sanxing.sesame.messaging;

import java.io.IOException;
import java.io.ObjectInput;
import javax.jbi.messaging.RobustInOnly;

public class RobustInOnlyImpl extends MessageExchangeImpl implements
		RobustInOnly {
	private static final long serialVersionUID = -1606399168587959356L;
	private static final int[][] STATES_CONSUMER = { { 593, 1, -1, -1, -1 },
			{ 0, -1, 2, 3, 3 }, { 912, -1, -1, 4, 4 }, { 512, -1, -1, -1, -1 },
			{ 0, -1, -1, -1, -1 } };

	private static final int[][] STATES_PROVIDER = { { 8, 1, -1, -1, -1 },
			{ 988, -1, 2, 4, 4 }, { 8, -1, -1, 3, 3 }, { 520, -1, -1, -1, -1 },
			{ 8, -1, -1, -1, -1 } };

	public RobustInOnlyImpl() {
	}

	public RobustInOnlyImpl(String exchangeId) {
		super(exchangeId, MessageExchangeSupport.ROBUST_IN_ONLY,
				STATES_CONSUMER);
		this.mirror = new RobustInOnlyImpl(this);
	}

	public RobustInOnlyImpl(ExchangePacket packet) {
		super(packet, STATES_CONSUMER);
		this.mirror = new RobustInOnlyImpl(this);
	}

	protected RobustInOnlyImpl(RobustInOnlyImpl mep) {
		super(mep.packet, STATES_PROVIDER);
		this.mirror = mep;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.packet = new ExchangePacket();
		this.packet.readExternal(in);
		if (this.packet.in != null) {
			this.packet.in.exchange = this;
		}
		if (this.packet.out != null) {
			this.packet.out.exchange = this;
		}
		if (this.packet.fault != null) {
			this.packet.fault.exchange = this;
		}
		this.state = in.read();
		this.mirror = new RobustInOnlyImpl();
		this.mirror.mirror = this;
		this.mirror.packet = this.packet;
		this.mirror.state = in.read();
		boolean provider = in.readBoolean();
		if (provider) {
			this.states = STATES_PROVIDER;
			this.mirror.states = STATES_CONSUMER;
		} else {
			this.states = STATES_CONSUMER;
			this.mirror.states = STATES_PROVIDER;
		}
	}
}