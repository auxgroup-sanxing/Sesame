package com.sanxing.sesame.messaging;

import com.sanxing.sesame.component.CopyTransformer;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Transaction;
import javax.xml.namespace.QName;

public class ExchangePacket implements Externalizable {
	private static final long serialVersionUID = -9110837382914609624L;
	protected URI pattern;
	protected String exchangeId;
	protected ComponentNameSpace destinationId;
	protected ComponentNameSpace sourceId;
	protected ExchangeStatus status = ExchangeStatus.ACTIVE;
	protected QName serviceName;
	protected QName interfaceName;
	protected QName operationName;
	protected Exception error;
	protected Map properties;
	protected NormalizedMessageImpl in;
	protected NormalizedMessageImpl out;
	protected FaultImpl fault;
	protected ServiceEndpoint endpoint;
	protected transient Transaction transactionContext;
	protected Boolean persistent;
	protected boolean aborted;

	public ExchangePacket() {
	}

	public ExchangePacket(ExchangePacket packet) throws MessagingException {
		this.destinationId = packet.destinationId;
		this.endpoint = null;
		this.error = null;
		this.exchangeId = null;
		this.interfaceName = packet.interfaceName;
		CopyTransformer ct = new CopyTransformer();
		if (packet.in != null) {
			this.in = new NormalizedMessageImpl();
			ct.transform(null, packet.in, this.in);
		}
		if (packet.out != null) {
			this.out = new NormalizedMessageImpl();
			ct.transform(null, packet.out, this.out);
		}
		if (packet.fault != null) {
			this.fault = new FaultImpl();
			ct.transform(null, packet.fault, this.fault);
		}
		this.operationName = packet.operationName;
		this.pattern = packet.pattern;
		if ((packet.properties != null) && (packet.properties.size() > 0)) {
			getProperties().putAll(packet.properties);
		}
		this.serviceName = packet.serviceName;
		this.sourceId = packet.sourceId;
		this.status = packet.status;
		this.transactionContext = packet.transactionContext;
		this.persistent = packet.persistent;
	}

	public ServiceEndpoint getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(ServiceEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public Transaction getTransactionContext() {
		return this.transactionContext;
	}

	public void setTransactionContext(Transaction transactionContext) {
		this.transactionContext = transactionContext;
	}

	public QName getInterfaceName() {
		return this.interfaceName;
	}

	public void setInterfaceName(QName interfaceName) {
		this.interfaceName = interfaceName;
	}

	public QName getOperationName() {
		return this.operationName;
	}

	public void setOperationName(QName operationName) {
		this.operationName = operationName;
	}

	public QName getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(QName serviceName) {
		this.serviceName = serviceName;
	}

	public void setStatus(ExchangeStatus status) {
		this.status = status;
	}

	public ExchangeStatus getStatus() {
		return this.status;
	}

	public URI getPattern() {
		return this.pattern;
	}

	public void setPattern(URI pattern) {
		this.pattern = pattern;
	}

	public Exception getError() {
		return this.error;
	}

	public void setError(Exception error) {
		this.error = error;
		this.status = ExchangeStatus.ERROR;
	}

	public String getExchangeId() {
		return this.exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public Map getProperties() {
		if (this.properties == null) {
			this.properties = new HashMap();
		}
		return this.properties;
	}

	public Object getProperty(String name) {
		if (this.properties != null) {
			return this.properties.get(name);
		}
		return null;
	}

	public void setProperty(String name, Object value) {
		if (value == null) {
			if (this.properties != null)
				this.properties.remove(name);
		} else
			getProperties().put(name, value);
	}

	public Set getPropertyNames() {
		if (this.properties != null) {
			return Collections.unmodifiableSet(this.properties.keySet());
		}
		return Collections.EMPTY_SET;
	}

	public ComponentNameSpace getSourceId() {
		return this.sourceId;
	}

	public void setSourceId(ComponentNameSpace sourceId) {
		this.sourceId = sourceId;
	}

	public ComponentNameSpace getDestinationId() {
		return this.destinationId;
	}

	public void setDestinationId(ComponentNameSpace destinationId) {
		this.destinationId = destinationId;
	}

	public Fault getFault() {
		return this.fault;
	}

	public void setFault(FaultImpl fault) {
		this.fault = fault;
	}

	public NormalizedMessage getIn() {
		return this.in;
	}

	public void setIn(NormalizedMessageImpl in) {
		this.in = in;
	}

	public NormalizedMessage getOut() {
		return this.out;
	}

	public void setOut(NormalizedMessageImpl out) {
		this.out = out;
	}

	public String toString() {
		return "ExchangePacket[: id=" + this.exchangeId + ", serviceDest="
				+ this.serviceName + ",endpoint=" + this.endpoint + "]";
	}

	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeUTF(this.pattern.toString());
		output.writeUTF((this.exchangeId != null) ? this.exchangeId : "");
		output.writeUTF(this.status.toString());
		output.writeObject(this.destinationId);
		output.writeObject(this.sourceId);
		output.writeObject(this.serviceName);
		output.writeObject(this.interfaceName);
		output.writeObject(this.operationName);
		output.writeObject(this.error);
		output.writeObject(this.properties);
		output.writeObject(this.in);
		output.writeObject(this.out);
		output.writeObject(this.fault);
		output.writeObject(this.endpoint);
		output.writeByte(this.persistent == null ? 0 : (this.persistent.booleanValue() ? 1 : 2));
	}

	public void readExternal(ObjectInput input) throws IOException,
			ClassNotFoundException {
		this.pattern = URI.create(input.readUTF());
		this.exchangeId = input.readUTF();
		this.status = ExchangeStatus.valueOf(input.readUTF());
		this.destinationId = ((ComponentNameSpace) input.readObject());
		this.sourceId = ((ComponentNameSpace) input.readObject());
		this.serviceName = ((QName) input.readObject());
		this.interfaceName = ((QName) input.readObject());
		this.operationName = ((QName) input.readObject());
		this.error = ((Exception) input.readObject());
		this.properties = ((Map) input.readObject());
		this.in = ((NormalizedMessageImpl) input.readObject());
		this.out = ((NormalizedMessageImpl) input.readObject());
		this.fault = ((FaultImpl) input.readObject());
		this.endpoint = ((ServiceEndpoint) input.readObject());
		byte p = input.readByte();
		this.persistent = ((p == 1) ? Boolean.TRUE : (p == 0) ? null
				: Boolean.FALSE);
	}

	public ExchangePacket copy() throws MessagingException {
		return new ExchangePacket(this);
	}

	public Boolean getPersistent() {
		return this.persistent;
	}

	public void setPersistent(Boolean persistent) {
		this.persistent = persistent;
	}

	public boolean isAborted() {
		return this.aborted;
	}

	public void setAborted(boolean timedOut) {
		this.aborted = timedOut;
	}

	public byte[] getData() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(buffer);
		os.writeObject(this);
		os.close();
		return buffer.toByteArray();
	}

	public static ExchangePacket readPacket(byte[] data) throws IOException,
			ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
				data));
		return ((ExchangePacket) ois.readObject());
	}
}