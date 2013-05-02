package com.sanxing.sesame.util;

import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.jaxp.StringSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

public class MessageCopier {
	private boolean copySubject;
	private boolean copyContent;
	private boolean copyProperties;
	private boolean copyAttachments;

	public MessageCopier() {
		this(true, true, true, true);
	}

	public MessageCopier(boolean copySubject, boolean copyContent,
			boolean copyProperties, boolean copyAttachments) {
		this.copySubject = copySubject;
		this.copyContent = copyContent;
		this.copyProperties = copyProperties;
		this.copyAttachments = copyAttachments;
	}

	public NormalizedMessage copy(NormalizedMessage message)
			throws MessagingException {
		NormalizedMessage copy = new MessageUtil.NormalizedMessageImpl();
		if (this.copySubject) {
			copySubject(message, copy);
		}
		if (this.copyContent) {
			copyContent(message, copy);
		}
		if (this.copyProperties) {
			copyProperties(message, copy);
		}
		if (this.copyAttachments) {
			copyAttachments(message, copy);
		}
		return copy;
	}

	public boolean isCopyAttachments() {
		return this.copyAttachments;
	}

	public boolean isCopyContent() {
		return this.copyContent;
	}

	public boolean isCopyProperties() {
		return this.copyProperties;
	}

	public boolean isCopySubject() {
		return this.copySubject;
	}

	private static void copySubject(NormalizedMessage from, NormalizedMessage to) {
		to.setSecuritySubject(from.getSecuritySubject());
	}

	private static void copyContent(NormalizedMessage from, NormalizedMessage to)
			throws MessagingException {
		String str = null;
		try {
			str = new SourceTransformer().toString(from.getContent());
		} catch (Exception e) {
			throw new MessagingException(e);
		}
		if (str != null)
			to.setContent(new StringSource(str));
	}

	private static void copyProperties(NormalizedMessage from,
			NormalizedMessage to) {
		for (Iterator i$ = from.getPropertyNames().iterator(); i$.hasNext();) {
			Object name = i$.next();
			to.setProperty((String) name, from.getProperty((String) name));
		}
	}

	private static void copyAttachments(NormalizedMessage from,
			NormalizedMessage to) throws MessagingException {
		for (Iterator i$ = from.getAttachmentNames().iterator(); i$.hasNext();) {
			Object name = i$.next();
			DataHandler handler = from.getAttachment((String) name);
			DataSource source = handler.getDataSource();
			if (!(source instanceof ByteArrayDataSource)) {
				DataSource copy = copyDataSource(source);
				handler = new DataHandler(copy);
			}
			to.addAttachment((String) name, handler);
		}
	}

	private static DataSource copyDataSource(DataSource source)
			throws MessagingException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileUtil.copyInputStream(source.getInputStream(), baos);
			ByteArrayDataSource bads = new ByteArrayDataSource(
					baos.toByteArray(), source.getContentType());
			bads.setName(source.getName());
			return bads;
		} catch (IOException e) {
			throw new MessagingException(e);
		}
	}
}