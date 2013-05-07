package com.sanxing.sesame.messaging;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.mbean.ComponentContextImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.Comparator;
import java.util.Set;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Transaction;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public abstract class MessageExchangeImpl implements MessageExchange,
		Externalizable {
	public static final String IN = "in";
	public static final String OUT = "out";
	public static final String FAULT = "fault";
	public static final int MAX_MSG_DISPLAY_SIZE = 1500;
	public static final boolean PRESERVE_CONTENT = Boolean
			.getBoolean("sesame.exchange.preserveContent");
	public static final int SYNC_STATE_ASYNC = 0;
	public static final int SYNC_STATE_SYNC_SENT = 1;
	public static final int SYNC_STATE_SYNC_RECEIVED = 2;
	public static final int TX_STATE_NONE = 0;
	public static final int TX_STATE_ENLISTED = 1;
	public static final int TX_STATE_CONVEYED = 2;
	protected static final int CAN_SET_IN_MSG = 1;
	protected static final int CAN_SET_OUT_MSG = 2;
	protected static final int CAN_SET_FAULT_MSG = 4;
	protected static final int CAN_PROVIDER = 8;
	protected static final int CAN_CONSUMER = 0;
	protected static final int CAN_SEND = 16;
	protected static final int CAN_STATUS_ACTIVE = 64;
	protected static final int CAN_STATUS_DONE = 128;
	protected static final int CAN_STATUS_ERROR = 256;
	protected static final int CAN_OWNER = 512;
	protected static final int STATES_CANS = 0;
	protected static final int STATES_NEXT_OUT = 1;
	protected static final int STATES_NEXT_FAULT = 2;
	protected static final int STATES_NEXT_ERROR = 3;
	protected static final int STATES_NEXT_DONE = 4;
	private static final long serialVersionUID = -3639175136897005605L;
	private static final Logger LOG = LoggerFactory.getLogger(MessageExchangeImpl.class);
	protected ComponentContextImpl sourceContext;
	protected ExchangePacket packet;
	protected PojoMarshaler marshaler;
	protected int state;
	protected int syncState = 0;

	protected int txState = 0;
	protected int[][] states;
	protected MessageExchangeImpl mirror;
	protected transient boolean pushDeliver;
	protected transient Object txLock;
	protected transient String key;

	public MessageExchangeImpl(String exchangeId, URI pattern, int[][] states) {
		this.states = states;
		this.packet = new ExchangePacket();
		this.packet.setExchangeId(exchangeId);
		this.packet.setPattern(pattern);
	}

	protected MessageExchangeImpl(ExchangePacket packet, int[][] states) {
		this.states = states;
		this.packet = packet;
	}

	protected MessageExchangeImpl() {
	}

	protected void copyFrom(MessageExchangeImpl me) {
		if (this != me) {
			this.packet = me.packet;
			this.state = me.state;
			this.mirror.packet = me.packet;
			this.mirror.state = me.mirror.state;
		}
	}

	protected boolean can(int c) {
		return ((this.states[this.state][0] & c) == c);
	}

	public ActivationSpec getActivationSpec() {
		if (this.sourceContext != null) {
			return this.sourceContext.getActivationSpec();
		}
		return null;
	}

	public ComponentContextImpl getSourceContext() {
		return this.sourceContext;
	}

	public void setSourceContext(ComponentContextImpl sourceContext) {
		this.sourceContext = sourceContext;
		this.mirror.sourceContext = sourceContext;
	}

	public ExchangePacket getPacket() {
		return this.packet;
	}

	public URI getPattern() {
		return this.packet.getPattern();
	}

	public String getExchangeId() {
		return this.packet.getExchangeId();
	}

	public ExchangeStatus getStatus() {
		if (this.packet.isAborted()) {
			return ExchangeStatus.ERROR;
		}
		return this.packet.getStatus();
	}

	public void setStatus(ExchangeStatus exchangeStatus)
			throws MessagingException {
		if (!(can(512))) {
			throw new IllegalStateException("component is not owner");
		}
		this.packet.setStatus(exchangeStatus);
	}

	public void setError(Exception exception) {
		if (!(can(512))) {
			throw new IllegalStateException(
					"component is not owner when trying to set error: "
							+ exception, exception);
		}
		this.packet.setError(exception);
	}

	public Exception getError() {
		return this.packet.getError();
	}

	public Fault getFault() {
		return this.packet.getFault();
	}

	public void setFault(Fault fault) throws MessagingException {
		setMessage(fault, "fault");
	}

	public NormalizedMessage createMessage() throws MessagingException {
		return new NormalizedMessageImpl(this);
	}

	public Fault createFault() throws MessagingException {
		return new FaultImpl();
	}

	public NormalizedMessage getMessage(String name) {
		if ("in".equals(name))
			return this.packet.getIn();
		if ("out".equals(name))
			return this.packet.getOut();
		if ("fault".equals(name)) {
			return this.packet.getFault();
		}
		return null;
	}

	public void setMessage(NormalizedMessage message, String name)
			throws MessagingException {
		if (!(can(512))) {
			throw new IllegalStateException("component is not owner");
		}
		if (message == null) {
			throw new IllegalArgumentException("message should not be null");
		}
		if (name == null) {
			throw new IllegalArgumentException("name should not be null");
		}
		if ("in".equalsIgnoreCase(name)) {
			if (!(can(1))) {
				throw new MessagingException("In not supported");
			}
			if (this.packet.getIn() != null) {
				throw new MessagingException("In message is already set");
			}
			((NormalizedMessageImpl) message).exchange = this;
			this.packet.setIn((NormalizedMessageImpl) message);
		} else if ("out".equalsIgnoreCase(name)) {
			if (!(can(2))) {
				throw new MessagingException("Out not supported");
			}
			if (this.packet.getOut() != null) {
				throw new MessagingException("Out message is already set");
			}
			((NormalizedMessageImpl) message).exchange = this;
			this.packet.setOut((NormalizedMessageImpl) message);
		} else if ("fault".equalsIgnoreCase(name)) {
			if (!(can(4))) {
				throw new MessagingException("Fault not supported");
			}
			if (!(message instanceof Fault)) {
				throw new MessagingException(
						"Setting fault, but message is not a fault");
			}
			if (this.packet.getFault() != null) {
				throw new MessagingException("Fault message is already set");
			}
			((NormalizedMessageImpl) message).exchange = this;
			this.packet.setFault((FaultImpl) message);
		} else {
			throw new MessagingException(
					"Message name must be in, out or fault");
		}
	}

	public Object getProperty(String name) {
		if ("javax.jbi.transaction.jta".equals(name))
			return this.packet.getTransactionContext();
		if ("com.sanxing.sesame.persistent".equals(name)) {
			return this.packet.getPersistent();
		}
		return this.packet.getProperty(name);
	}

	public void setProperty(String name, Object value) {
		if (!(can(512))) {
			throw new IllegalStateException("component is not owner");
		}
		if (name == null) {
			throw new IllegalArgumentException("name should not be null");
		}
		if ("javax.jbi.transaction.jta".equals(name))
			this.packet.setTransactionContext((Transaction) value);
		else if ("com.sanxing.sesame.persistent".equals(name))
			this.packet.setPersistent((Boolean) value);
		else
			this.packet.setProperty(name, value);
	}

	public Set getPropertyNames() {
		return this.packet.getPropertyNames();
	}

	public void setEndpoint(ServiceEndpoint endpoint) {
		this.packet.setEndpoint(endpoint);
	}

	public void setService(QName name) {
		this.packet.setServiceName(name);
	}

	public void setOperation(QName name) {
		this.packet.setOperationName(name);
	}

	public void setInterfaceName(QName name) {
		this.packet.setInterfaceName(name);
	}

	public ServiceEndpoint getEndpoint() {
		return this.packet.getEndpoint();
	}

	public QName getService() {
		return this.packet.getServiceName();
	}

	public QName getInterfaceName() {
		return this.packet.getInterfaceName();
	}

	public QName getOperation() {
		return this.packet.getOperationName();
	}

	public Transaction getTransactionContext() {
		return this.packet.getTransactionContext();
	}

	public void setTransactionContext(Transaction transaction)
			throws MessagingException {
		this.packet.setTransactionContext(transaction);
	}

	public boolean isTransacted() {
		return (this.packet.getTransactionContext() != null);
	}

	public MessageExchange.Role getRole() {
		return ((can(8)) ? MessageExchange.Role.PROVIDER
				: MessageExchange.Role.CONSUMER);
	}

	public NormalizedMessage getInMessage() {
		return this.packet.getIn();
	}

	public void setInMessage(NormalizedMessage message)
			throws MessagingException {
		setMessage(message, "in");
	}

	public NormalizedMessage getOutMessage() {
		return getMessage("out");
	}

	public void setOutMessage(NormalizedMessage message)
			throws MessagingException {
		setMessage(message, "out");
	}

	public ComponentNameSpace getSourceId() {
		return this.packet.getSourceId();
	}

	public void setSourceId(ComponentNameSpace sourceId) {
		this.packet.setSourceId(sourceId);
	}

	public ComponentNameSpace getDestinationId() {
		return this.packet.getDestinationId();
	}

	public void setDestinationId(ComponentNameSpace destinationId) {
		this.packet.setDestinationId(destinationId);
	}

	public Boolean getPersistent() {
		return this.packet.getPersistent();
	}

	public void setPersistent(Boolean persistent) {
		this.packet.setPersistent(persistent);
	}

	public PojoMarshaler getMarshaler() {
		if (this.marshaler == null) {
			this.marshaler = new DefaultMarshaler();
		}
		return this.marshaler;
	}

	public void setMarshaler(PojoMarshaler marshaler) {
		this.marshaler = marshaler;
	}

	public abstract void readExternal(ObjectInput paramObjectInput)
			throws IOException, ClassNotFoundException;

	public void writeExternal(ObjectOutput out) throws IOException {
		this.packet.writeExternal(out);
		out.write(this.state);
		out.write(this.mirror.state);
		out.writeBoolean(can(8));
	}

	public void handleSend(boolean sync) throws MessagingException {
		if (!(can(16))) {
			throw new MessagingException("illegal call to send / sendSync");
		}
		if ((sync) && (getStatus() != ExchangeStatus.ACTIVE)) {
			throw new MessagingException("illegal call to sendSync");
		}
		this.syncState = ((sync) ? 1 : 0);

		ExchangeStatus status = getStatus();
		if ((status == ExchangeStatus.ACTIVE) && (!(can(64)))) {
			throw new MessagingException("illegal exchange status: active");
		}
		if ((status == ExchangeStatus.DONE) && (!(can(128)))) {
			throw new MessagingException("illegal exchange status: done");
		}
		if ((status == ExchangeStatus.ERROR) && (!(can(256)))) {
			throw new MessagingException("illegal exchange status: error");
		}

		if ((status == ExchangeStatus.ACTIVE)
				&& (this.packet.getFault() == null))
			this.state = this.states[this.state][1];
		else if ((status == ExchangeStatus.ACTIVE)
				&& (this.packet.getFault() != null))
			this.state = this.states[this.state][2];
		else if (status == ExchangeStatus.ERROR)
			this.state = this.states[this.state][3];
		else if (status == ExchangeStatus.DONE)
			this.state = this.states[this.state][4];
		else {
			throw new IllegalStateException("unknown status");
		}
		if ((this.state < 0) || (this.state >= this.states.length))
			throw new IllegalStateException("next state is illegal");
	}

	public void handleAccept() throws MessagingException {
		ExchangeStatus status = getStatus();
		int nextState;
		if ((status == ExchangeStatus.ACTIVE)
				&& (this.packet.getFault() == null)) {
			nextState = this.states[this.state][1];
		} else {
			if ((status == ExchangeStatus.ACTIVE)
					&& (this.packet.getFault() != null)) {
				nextState = this.states[this.state][2];
			} else {
				if (status == ExchangeStatus.ERROR) {
					nextState = this.states[this.state][3];
				} else {
					if (status == ExchangeStatus.DONE)
						nextState = this.states[this.state][4];
					else
						throw new IllegalStateException("unknown status");
				}
			}
		}
		if ((nextState < 0) || (nextState >= this.states.length)) {
			throw new IllegalStateException("next state is illegal");
		}
		this.state = nextState;
	}

	public MessageExchangeImpl getMirror() {
		return this.mirror;
	}

	public int getSyncState() {
		return this.syncState;
	}

	public void setSyncState(int syncState) {
		this.syncState = syncState;
	}

	public int getTxState() {
		return this.txState;
	}

	public void setTxState(int txState) {
		this.txState = txState;
	}

	public boolean isPushDelivery() {
		return this.pushDeliver;
	}

	public void setPushDeliver(boolean b) {
		this.pushDeliver = true;
	}

	public Object getTxLock() {
		return this.txLock;
	}

	public void setTxLock(Object txLock) {
		this.txLock = txLock;
	}

	public String toString() {
		try {
			StringBuffer sb = new StringBuffer();
			String name = super.getClass().getName();
			name = name.substring(name.lastIndexOf(46) + 1, name.length() - 4);
			sb.append(name);
			sb.append("[\n");
			sb.append("  id: ").append(getExchangeId()).append('\n');
			sb.append("  status: ").append(getStatus()).append('\n');
			sb.append("  role: ")
					.append((getRole() == MessageExchange.Role.CONSUMER) ? "consumer"
							: "provider").append('\n');
			if (getInterfaceName() != null) {
				sb.append("  interface: ").append(getInterfaceName())
						.append('\n');
			}
			if (getService() != null) {
				sb.append("  service: ").append(getService()).append('\n');
			}
			if (getEndpoint() != null) {
				sb.append("  endpoint: ")
						.append(getEndpoint().getEndpointName()).append('\n');
			}
			if (getOperation() != null) {
				sb.append("  operation: ").append(getOperation()).append('\n');
			}
			SourceTransformer st = new SourceTransformer();
			display("in", sb, st);
			display("out", sb, st);
			display("fault", sb, st);
			if (getError() != null) {
				sb.append("  error: ");
				sb.append(getError());
				sb.append('\n');
			}
			sb.append("]");
			return sb.toString();
		} catch (Exception e) {
			LOG.trace("Error caught in toString", e);
		}
		return super.toString();
	}

	private void display(String msg, StringBuffer sb, SourceTransformer st) {
		if (getMessage(msg) != null) {
			sb.append("  ").append(msg).append(": ");
			try {
				if (getMessage(msg).getContent() != null) {
					if (PRESERVE_CONTENT) {
						sb.append(getMessage(msg).getContent().getClass());
					} else {
						Node node = st.toDOMNode(getMessage(msg).getContent());

						String str = st.toString(node);
						if (str.length() > 1500)
							sb.append(str.substring(0, 1500)).append("...");
						else
							sb.append(str);
					}
				} else
					sb.append("null");
			} catch (Exception e) {
				sb.append("Unable to display: ").append(e);
			}
			sb.append('\n');
		}
	}

	public String getKey() {
		if (this.key == null) {
			this.key = ((getRole() == MessageExchange.Role.CONSUMER) ? "consumer:"
					: "provider:")
					+ getExchangeId();
		}
		return this.key;
	}

	public static class AgeComparator implements
			Comparator<MessageExchangeImpl> {
		public int compare(MessageExchangeImpl m0, MessageExchangeImpl m1) {
			int i0 = m0.state * 4 / (m0.states.length - 1);
			int i1 = m1.state * 4 / (m1.states.length - 1);
			if (i0 < i1)
				return 1;
			if (i0 == i1) {
				return 0;
			}
			return -1;
		}
	}
}