package com.sanxing.sesame.binding.transport;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.Carrier;
import com.sanxing.sesame.binding.context.MessageContext;
import java.io.IOException;
import java.net.URI;
import org.w3c.dom.Element;

public class TransportProxy implements Acceptor, Connector {
	private Transport transport;
	private URI uri;
	private long ref = 0L;

	protected TransportProxy(Transport transport, URI uri, Element config)
			throws IOException {
		this.transport = transport;
		this.transport.setURI(uri);
		this.transport.init(config);
		this.transport.open();

		this.uri = uri;
		this.ref = 1L;
	}

	protected Transport getReference() {
		this.ref += 1L;
		return this;
	}

	public void close() throws IOException {
		this.ref -= 1L;

		if (this.ref == 0L) {
			this.transport.close();
			String endpoint = this.uri.getScheme() + "://"
					+ this.uri.getAuthority();
			TransportFactory.remove(endpoint);
		}
	}

	public String getCharacterEncoding() {
		return this.transport.getCharacterEncoding();
	}

	public URI getURI() {
		return this.uri;
	}

	public boolean isActive() {
		return this.transport.isActive();
	}

	public void init(Element config) {
		this.transport.init(config);
	}

	public void open() throws IOException {
		if (!(this.transport.isActive()))
			this.transport.open();
	}

	public void removeCarrier(String contextPath, Carrier receiver) {
		this.transport.removeCarrier(contextPath, receiver);
	}

	public void setConfig(String contextPath, Element config)
			throws IllegalArgumentException {
		this.transport.setConfig(contextPath, config);
	}

	public void addCarrier(String contextPath, Carrier receiver) {
		this.transport.addCarrier(contextPath, receiver);
	}

	public void setURI(URI uri) {
		throw new RuntimeException("Illegal access, user can not set uri");
	}

	public boolean getKeepAlive() {
		return this.transport.getKeepAlive();
	}

	public void setKeepAlive(boolean on) {
		this.transport.setKeepAlive(on);
	}

	public void reply(MessageContext context) throws BindingException,
			IOException {
		if (this.transport instanceof Connector)
			((Connector) this.transport).sendOut(context);
	}

	public void sendOut(MessageContext context) throws BindingException,
			IOException {
		if (this.transport instanceof Connector)
			((Connector) this.transport).sendOut(context);
	}

	public int hashCode() {
		return this.transport.hashCode();
	}
}