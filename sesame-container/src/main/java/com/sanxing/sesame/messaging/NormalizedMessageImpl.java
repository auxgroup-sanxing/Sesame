package com.sanxing.sesame.messaging;

import com.sanxing.sesame.exception.RuntimeJBIException;
import com.sanxing.sesame.jaxp.BytesSource;
import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.jaxp.StringSource;
import com.sanxing.sesame.util.ByteArrayDataSource;
import com.sanxing.sesame.util.FileUtil;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.security.auth.Subject;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

public class NormalizedMessageImpl implements NormalizedMessage, Externalizable {
	private static final long serialVersionUID = 9179194301410526549L;
	private static final SourceTransformer TRANSFORMER = new SourceTransformer();
	protected transient MessageExchangeImpl exchange;
	private transient Source content;
	private transient Object body;
	private Subject securitySubject;
	private Map properties;
	private Map attachments;

	public NormalizedMessageImpl() {
	}

	public NormalizedMessageImpl(MessageExchangeImpl exchange) {
		this.exchange = exchange;
	}

	public Source getContent() {
		if ((this.content == null) && (this.body != null)) {
			try {
				getMarshaler().marshal(this.exchange, this, this.body);
			} catch (MessagingException e) {
				throw new RuntimeJBIException(e);
			}
		}
		return this.content;
	}

	public void setContent(Source source) {
		this.content = source;
	}

	public Subject getSecuritySubject() {
		return this.securitySubject;
	}

	public void setSecuritySubject(Subject securitySubject) {
		this.securitySubject = securitySubject;
	}

	public Object getProperty(String name) {
		if (this.properties != null) {
			return this.properties.get(name);
		}
		return null;
	}

	public Set getPropertyNames() {
		if (this.properties != null) {
			return Collections.unmodifiableSet(this.properties.keySet());
		}
		return Collections.EMPTY_SET;
	}

	public void setProperty(String name, Object value) {
		if (value == null) {
			if (this.properties != null)
				this.properties.remove(name);
		} else
			getProperties().put(name, value);
	}

	public void addAttachment(String id, DataHandler handler) {
		getAttachments().put(id, handler.getDataSource());
	}

	public DataHandler getAttachment(String id) {
		if ((this.attachments != null) && (this.attachments.get(id) != null)) {
			return new DataHandler((DataSource) this.attachments.get(id));
		}
		return null;
	}

	public Iterator listAttachments() {
		if (this.attachments != null) {
			return this.attachments.keySet().iterator();
		}
		return Collections.EMPTY_LIST.iterator();
	}

	public void removeAttachment(String id) {
		if (this.attachments != null)
			this.attachments.remove(id);
	}

	public Set getAttachmentNames() {
		if (this.attachments != null) {
			return Collections.unmodifiableSet(this.attachments.keySet());
		}
		return Collections.EMPTY_SET;
	}

	public String toString() {
		return super.toString() + "{properties: " + getProperties() + "}";
	}

	public Object getBody() throws MessagingException {
		if (this.body == null) {
			this.body = getMarshaler().unmarshal(this.exchange, this);
		}
		return this.body;
	}

	public Object getBody(PojoMarshaler marshaler) throws MessagingException {
		return marshaler.unmarshal(this.exchange, this);
	}

	public void setBody(Object body) throws MessagingException {
		this.body = body;
	}

	public String getBodyText() throws TransformerException {
		return TRANSFORMER.toString(getContent());
	}

	public void setBodyText(String xml) {
		setContent(new StringSource(xml));
	}

	public PojoMarshaler getMarshaler() {
		return this.exchange.getMarshaler();
	}

	public MessageExchange getExchange() {
		return this.exchange;
	}

	public Fault createFault() throws MessagingException {
		return getExchange().createFault();
	}

	protected Map getProperties() {
		if (this.properties == null) {
			this.properties = createPropertiesMap();
		}
		return this.properties;
	}

	protected Map getAttachments() {
		if (this.attachments == null) {
			this.attachments = createAttachmentsMap();
		}
		return this.attachments;
	}

	protected void setAttachments(Map attachments) {
		this.attachments = attachments;
	}

	protected void setProperties(Map properties) {
		this.properties = properties;
	}

	protected Map createPropertiesMap() {
		return new HashMap();
	}

	protected Map createAttachmentsMap() {
		return new HashMap();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		try {
			convertAttachments();
			out.writeObject(this.attachments);
			out.writeObject(this.properties);
			String src = TRANSFORMER.toString(this.content);
			out.writeObject(src);

			if ((((this.content instanceof StreamSource) || (this.content instanceof SAXSource)))
					&& (!(this.content instanceof StringSource))
					&& (!(this.content instanceof BytesSource))) {
				this.content = new StringSource(src);
			}
		} catch (TransformerException e) {
			throw ((IOException) new IOException(
					"Could not transform content to string").initCause(e));
		}
	}

	private void convertAttachments() throws IOException {
		if (this.attachments != null) {
			Map newAttachments = createAttachmentsMap();
			for (Iterator it = this.attachments.keySet().iterator(); it
					.hasNext();) {
				String name = (String) it.next();
				DataSource ds = (DataSource) this.attachments.get(name);
				if (ds instanceof ByteArrayDataSource) {
					newAttachments.put(name, ds);
				} else {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					FileUtil.copyInputStream(ds.getInputStream(), baos);
					ByteArrayDataSource bads = new ByteArrayDataSource(
							baos.toByteArray(), ds.getContentType());
					bads.setName(ds.getName());
					newAttachments.put(name, bads);
				}
			}
			this.attachments = newAttachments;
		}
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.attachments = ((Map) in.readObject());
		this.properties = ((Map) in.readObject());
		String src = (String) in.readObject();
		if (src != null)
			this.content = new StringSource(src);
	}
}