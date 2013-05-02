package com.sanxing.sesame.transport.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import javax.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameMailClient {
	private String recvProtocol = "pop3";
	private String sendProtocol = "smtp";

	private String popServer;
	private Session recvSession;
	private String recvDebug = "true";
	private String recvAuth = "true";
	private String recvUser;
	private String recvPassword;

	private String from;
	private String to;
	private String smtpServer;
	private Session sendSession;
	private String sendDebug = "true";
	private String sendAuth = "true";
	private String sendUser;
	private String sendPassword;

	private String attachment = "false";

	private Store store = null;
	private Folder folder = null;

	private static Logger LOG = LoggerFactory.getLogger(SesameMailClient.class);

	public void setBindingProperties(Map<String, String> props) {
	}

	public void initSession() throws Exception {
	}

	public void sendMail(byte[] resp, String subject) throws Exception {
		if (this.sendSession == null)
			throw new Exception("smtp server do not init,session is null!");
		MimeMessage msg = new MimeMessage(this.sendSession);

		msg.setFrom(new InternetAddress(this.from));

		InternetAddress[] addresses = { new InternetAddress(this.to) };

		msg.setRecipients(MimeMessage.RecipientType.TO, addresses);

		msg.setSubject(subject);

		MimeBodyPart mbp2 = new MimeBodyPart();

		DataSource source = new ByteArrayDataSource(resp, "text/plain");
		mbp2.setDataHandler(new DataHandler(source));
		mbp2.setFileName(subject + ".txt");

		Multipart mp = new MimeMultipart();

		mp.addBodyPart(mbp2);

		msg.setContent(mp);

		Transport transport = this.sendSession.getTransport(this.sendProtocol);
		transport.connect(this.sendUser, this.sendPassword);
		transport.sendMessage(msg, msg.getAllRecipients());

		transport.close();
	}

	public byte[] getMailBySubject(String subject) throws Exception {
		byte[] result = (byte[]) null;
		if (this.recvSession == null)
			throw new Exception("pop server do not init!");
		Store store = this.recvSession.getStore(this.recvProtocol);
		store.connect();
		if (store.isConnected()) {
			Folder folder = store.getFolder("INBOX");

			folder.open(2);

			SearchTerm subTerm = new SubjectTerm(subject);
			Message[] msgs = folder.search(subTerm);
			Message[] arrayOfMessage1;
			if ((arrayOfMessage1 = msgs).length != 0) {
				Message msg = arrayOfMessage1[0];
				result = processOneMail(msg);
				msg.setFlag(Flag.DELETED, true);
			}

			folder.close(true);
			store.close();
		}

		return result;
	}

	public byte[] processOneMail(Message msg) throws Exception {
		byte[] result = (byte[]) null;
		if (this.attachment.equals("false"))
			result = processOneMailNoAttachment(msg);
		else {
			result = processOneMailWithAttachment(msg);
		}

		if (result != null) {
			msg.setFlag(Flag.DELETED, true);
		}
		return result;
	}

	public byte[] processOneMailNoAttachment(Message msg) throws Exception {
		byte[] result = (byte[]) null;

		if (msg.isMimeType("multipart/alternative")) {
			Multipart part = (MimeMultipart) msg.getContent();
			int i = 0;
			for (int n = part.getCount(); i < n; ++i) {
				BodyPart body = part.getBodyPart(i);

				if (!(body.isMimeType("text/plain")))
					continue;
				String content = (String) body.getContent();

				LOG.debug("body type is:" + body.getContentType() + "\n"
						+ "content is:[" + content + "],len is:"
						+ content.getBytes().length);

				result = content.getBytes();
			}
		} else if (msg.isMimeType("text/plain")) {
			String content = (String) msg.getContent();
			result = content.getBytes();
		} else if (msg.isMimeType("text/html")) {
			LOG.error("html 格式");
		} else {
			throw new Exception("邮件包含附件");
		}
		return result;
	}

	public byte[] processOneMailWithAttachment(Message msg) throws Exception {
		byte[] result = (byte[]) null;
		if (msg.isMimeType("multipart/mixed")) {
			Multipart multipart = (Multipart) msg.getContent();
			int i = 0;
			for (int n = multipart.getCount(); i < n; ++i) {
				Part part = multipart.getBodyPart(i);
				String disposition = part.getDisposition();
				LOG.debug("part type is:" + part.getContentType());
				if (disposition != null) {
					if (disposition.equals("attachment")) {
						if (part.isMimeType("text/plain")) {
							LOG.debug("part type is:" + part.getContentType());
							String content = (String) part.getContent();

							result = new String(content.getBytes("iso-8859-1"),
									"gbk").getBytes();
							LOG.debug("content is:" + new String(result)
									+ ",filename is:" + part.getFileName());
							continue;
						}
						if ((part.isMimeType("application/octet-stream"))
								|| (part.isMimeType("application/x-zip-compressed"))) {
							LOG.debug("part type is:" + part.getContentType());
							InputStream in = part.getInputStream();
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							byte[] temp = new byte[1024];
							int len = 0;
							while ((len = in.read(temp)) != -1) {
								out.write(temp, 0, len);
								len = 0;
							}
							in.close();
							result = out.toByteArray();
							out.close();
							continue;
						}

						throw new Exception("未知类型:[" + part.getContentType()
								+ "]");
					}

					if (disposition.equals("inline")) {
						continue;
					}
					throw new Exception("未知部署方式：[" + disposition + "]");
				}
			}
		} else if (!(msg.isMimeType("multipart/related"))) {
			throw new Exception("邮件未包含附件");
		}

		return result;
	}

	public void disconnecteStore() throws Exception {
		if (this.store == null)
			return;
		if (this.store.isConnected()) {
			this.folder.close(true);
			this.store.close();
		}
	}

	public void connecteStore() throws Exception {
		if ((this.store == null) || (!(this.store.isConnected())))
			return;
		this.store = this.recvSession.getStore(this.recvProtocol);
		this.store.connect();
		if (this.store.isConnected()) {
			this.folder = this.store.getFolder("INBOX");

			this.folder.open(2);
		}
	}

	public Message[] getAllMails() throws Exception {
		Message[] msgs = (Message[]) null;
		if (this.recvSession == null)
			throw new Exception("pop server do not init!");
		this.store = this.recvSession.getStore(this.recvProtocol);
		this.store.connect();
		if (this.store.isConnected()) {
			this.folder = this.store.getFolder("INBOX");

			this.folder.open(2);

			msgs = this.folder.getMessages();
		} else {
			throw new Exception("can not connect to service!");
		}

		return msgs;
	}

	public InputStream getEmails() throws Exception {
		InputStream in = null;
		Store store = this.recvSession.getStore(this.recvProtocol);
		store.connect();

		Folder folder = store.getFolder("INBOX");

		folder.open(2);
		Message[] msgs = folder.getMessages();

		byte[] getBuf = doMessages(msgs);
		if (getBuf != null) {
			in = new ByteArrayInputStream(getBuf);
		}
		folder.close(false);
		store.close();
		return in;
	}

	public byte[] doMessages(Message[] msgs) throws Exception {
		byte[] result = (byte[]) null;
		for (Message msg : msgs) {
			if (msg.getContentType().startsWith("multipart/")) {
				Multipart part = (Multipart) msg.getContent();
				int i = 0;
				for (int n = part.getCount(); i < n; ++i) {
					Part bp = part.getBodyPart(i);
					String disposition = bp.getDisposition();
					if ((disposition == null)
							|| ((!(disposition.equals("attachment"))) && (!(disposition
									.equals("inline"))))) {
						continue;
					}

					InputStream in = bp.getInputStream();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] temp = new byte[1024];
					int len = 0;
					while ((len = in.read(temp)) != -1) {
						out.write(temp, 0, len);
						len = 0;
					}
					in.close();

					result = out.toByteArray();
					out.close();
					break;
				}

			}

		}

		return result;
	}

	public void initPOPServer() throws Exception {
		Properties props = new Properties();
		props.put("mail.pop3.host", this.popServer);
		props.put("mail.pop3.auth", this.recvAuth);

		this.recvSession = Session.getDefaultInstance(props,
				new Authenticator() {
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								SesameMailClient.this.recvUser,
								SesameMailClient.this.recvPassword);
					}
				});
		if (this.recvDebug.equals("true"))
			this.recvSession.setDebug(true);
	}

	public void initSMTPServer() throws Exception {
		Properties props = new Properties();
		props = new Properties();
		props.put("mail.smtp.host", this.smtpServer);
		props.put("mail.smtp.auth", this.sendAuth);

		this.sendSession = Session.getInstance(props, null);
		if (this.sendDebug.equals("true"))
			this.sendSession.setDebug(true);
	}

	public void init() throws Exception {
		initPOPServer();
		initSMTPServer();
	}

	public void setProperties(Map<?, ?> props) {
		this.popServer = ((String) props.get("popServer"));
		this.recvDebug = ((String) props.get("recvDebug"));
		this.recvAuth = ((String) props.get("recvAuth"));
		this.recvUser = ((String) props.get("recvUser"));
		this.recvPassword = ((String) props.get("recvPassword"));

		this.from = ((String) props.get("from"));
		this.to = ((String) props.get("to"));
		this.smtpServer = ((String) props.get("smtpServer"));
		this.sendDebug = ((String) props.get("sendDebug"));
		this.sendAuth = ((String) props.get("sendAuth"));
		this.sendUser = ((String) props.get("sendUser"));
		this.sendPassword = ((String) props.get("sendPassword"));

		this.attachment = ((String) props.get("attachment"));
	}
}