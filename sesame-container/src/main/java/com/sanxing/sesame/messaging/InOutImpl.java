package com.sanxing.sesame.messaging;

import java.io.IOException;
import java.io.ObjectInput;
import javax.jbi.messaging.InOut;

public class InOutImpl extends MessageExchangeImpl implements InOut {
	private static final long serialVersionUID = -1639492357707831113L;
	private static final int[][] STATES_CONSUMER = { { 593, 1, -1, -1, -1 },
			{ 0, 2, 2, 3, 3 }, { 912, -1, -1, 4, 4 }, { 512, -1, -1, -1, -1 },
			{ 0, -1, -1, -1, -1 } };

	private static final int[][] STATES_PROVIDER = { { 8, 1, -1, -1 },
			{ 862, 2, 2, 3, -1 }, { 8, -1, -1, 4, 4 }, { 8, -1, -1, -1, -1 },
			{ 520, -1, -1, -1, -1 } };

	public InOutImpl() {
	}

	public InOutImpl(String exchangeId) {
		super(exchangeId, MessageExchangeSupport.IN_OUT, STATES_CONSUMER);
		this.mirror = new InOutImpl(this);
	}

	public InOutImpl(ExchangePacket packet) {
		super(packet, STATES_CONSUMER);
		this.mirror = new InOutImpl(this);
	}

	protected InOutImpl(InOutImpl mep) {
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
		this.mirror = new InOutImpl();
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