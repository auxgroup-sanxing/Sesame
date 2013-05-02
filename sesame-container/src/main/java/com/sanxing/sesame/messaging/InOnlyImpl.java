package com.sanxing.sesame.messaging;

import java.io.IOException;
import java.io.ObjectInput;
import javax.jbi.messaging.InOnly;

public class InOnlyImpl extends MessageExchangeImpl implements InOnly {
	private static final long serialVersionUID = -4851111881482457905L;
	private static final int[][] STATES_CONSUMER = { { 593, 1, -1, -1, -1 },
			{ 0, -1, -1, 2, 2 }, { 512, -1, -1, -1, -1 } };

	private static final int[][] STATES_PROVIDER = { { 8, 1, -1, -1, -1 },
			{ 920, -1, -1, 2, 2 }, { 8, -1, -1, -1, -1 } };

	public InOnlyImpl() {
	}

	public InOnlyImpl(String exchangeId) {
		super(exchangeId, MessageExchangeSupport.IN_ONLY, STATES_CONSUMER);
		this.mirror = new InOnlyImpl(this);
	}

	public InOnlyImpl(ExchangePacket packet) {
		super(packet, STATES_CONSUMER);
		this.mirror = new InOnlyImpl(this);
	}

	protected InOnlyImpl(InOnlyImpl mep) {
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
		this.mirror = new InOnlyImpl();
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