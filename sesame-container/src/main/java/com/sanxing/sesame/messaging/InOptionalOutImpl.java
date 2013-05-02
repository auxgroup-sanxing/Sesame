package com.sanxing.sesame.messaging;

import java.io.IOException;
import java.io.ObjectInput;
import javax.jbi.messaging.InOptionalOut;

public class InOptionalOutImpl extends MessageExchangeImpl implements
		InOptionalOut {
	private static final long serialVersionUID = -3145649037372074912L;
	private static final int[][] STATES_CONSUMER = { { 593, 1, -1, -1, -1 },
			{ 0, 2, 3, 4, 4 }, { 980, -1, 5, 6, 6 }, { 912, -1, -1, 6, 6 },
			{ 512, -1, -1, -1, -1 }, { 0, -1, -1, 7, 7 },
			{ 0, -1, -1, -1, -1 }, { 512, -1, -1, -1, -1 } };

	private static final int[][] STATES_PROVIDER = { { 8, 1, -1, -1 },
			{ 990, 2, 3, 4, 4 }, { 8, -1, 5, 6, 6 }, { 8, -1, -1, 6, 6 },
			{ 8, -1, -1, -1, -1 }, { 920, -1, -1, 7, 7 },
			{ 520, -1, -1, -1, -1 }, { 8, -1, -1, -1, -1 } };

	public InOptionalOutImpl() {
	}

	public InOptionalOutImpl(String exchangeId) {
		super(exchangeId, MessageExchangeSupport.IN_OPTIONAL_OUT,
				STATES_CONSUMER);
		this.mirror = new InOptionalOutImpl(this);
	}

	public InOptionalOutImpl(ExchangePacket packet) {
		super(packet, STATES_CONSUMER);
		this.mirror = new InOptionalOutImpl(this);
	}

	protected InOptionalOutImpl(InOptionalOutImpl mep) {
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
		this.mirror = new InOptionalOutImpl();
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