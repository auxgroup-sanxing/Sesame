package com.sanxing.studio.emu;

import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

public class TcpServer extends BindingUnit implements Server {
	private static final int DEFAULT_BUFFER_CAPACITY = 8192;
	private static final int DEFAULT_READ_LENGTH = 1024;
	protected static Logger LOG = LoggerFactory.getLogger(TcpServer.class);

	private Map<String, OperationEntry> provideEntries = new Hashtable();
	private ServerSocket transport;
	private Thread acceptThread;
	private ThreadPoolExecutor poolExecutor;
	private int buffer_capacity = 8192;
	private ByteOrder byteOrder;
	private String headEncoding;
	private boolean lengthIncludeHead;
	private int recvOffset;
	private int sendOffset;
	private int recvHeadLen;
	private int sendHeadLen;
	private Class<? extends Decoder> decodeClass;
	private Class<? extends Encoder> encodeClass;
	private InetSocketAddress socketAddress;

	public TcpServer(Map<?, ?> properties, File serviceUnitRoot)
			throws Exception {
		super(properties, serviceUnitRoot);
		this.poolExecutor = new ThreadPoolExecutor(10, 1000, 10000L,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue(10));
		loadBundles();
	}

	protected void setProperties(Map<?, ?> properties) throws Exception {
		super.setProperties(properties);
		this.byteOrder = ((getProperty("endian").equals("little")) ? ByteOrder.LITTLE_ENDIAN
				: ByteOrder.BIG_ENDIAN);
		this.headEncoding = getProperty("len_encoding");
		this.lengthIncludeHead = getProperty("len_include").equals("I");
		try {
			this.recvOffset = Integer.parseInt(getProperty("recv_len_begin"));
			this.recvHeadLen = (Integer.parseInt(getProperty("recv_len_end")) - this.recvOffset);
			this.sendOffset = Integer.parseInt(getProperty("send_len_begin"));
			this.sendHeadLen = (Integer.parseInt(getProperty("send_len_end")) - this.sendOffset);
		} catch (NumberFormatException e) {
			if (!(this.headEncoding.equals("0")))
				throw new Exception("Get head length failure: "
						+ e.getMessage(), e);
		}
		try {
			this.decodeClass = Class.forName(getProperty("decode-class"))
					.asSubclass(Decoder.class);
			this.encodeClass = Class.forName(getProperty("encode-class"))
					.asSubclass(Encoder.class);
		} catch (ClassNotFoundException e) {
			throw new Exception("Specified decode/encode class not found: "
					+ e.getMessage(), e);
		}
		try {
			this.buffer_capacity = Integer.parseInt(getProperty("buffer_size"));
		} catch (Throwable e) {
			LOG.warn("读取缓冲区大小失败，采用默认值");
		}
	}

	protected void loadBundles() throws JDOMException, IOException,
			WSDLException {
		SAXBuilder builder = new SAXBuilder();

		File unitFile = new File(getUnitRoot(), "META-INF/unit.xml");
		Element rootEl = builder.build(unitFile).getRootElement();
		String oriented = rootEl.getAttributeValue("oriented");
		String endpoint = rootEl.getAttributeValue("endpoint");

		File epFolder = new File(getUnitRoot(), "../../" + oriented + "/"
				+ endpoint);
		File wsdl = new File(epFolder, "META-INF/unit.wsdl");
		Definition definition = loadDefition(wsdl);
		Map portTypes = definition.getPortTypes();

		PortType intfEl;
		Iterator it;
		String uri = definition.getTargetNamespace();
		for (Iterator iter = portTypes.values().iterator(); iter.hasNext();) {
			intfEl = (PortType) iter.next();
			List opers = intfEl.getOperations();
			for (it = opers.iterator(); it.hasNext();) {
				Operation operEl = (Operation) it.next();
				String code = operEl.getName();
				OperationEntry entry = new OperationEntry();
				entry.setInterfaceName(intfEl.getQName().getLocalPart());
				String namespace = uri
						+ ((uri.endsWith("/")) ? code : new StringBuilder()
								.append("/").append(code).toString());
				entry.setSchema(schemaForNamespace(namespace));

				File file = new File(getUnitRoot(), code + ".xml");
				if (file.isFile()) {
					entry.setData(builder.build(file));
				}
				this.provideEntries.put(code, entry);
			}
		}
	}

	public void run() {
		LOG.debug("listening at: " + this.socketAddress);
		while ((this.transport != null) && (!(this.transport.isClosed()))) {
			try {
				Socket socket = this.transport.accept();
				StreamChannel chnn = new StreamChannel(socket.getInputStream(),
						socket.getOutputStream()) {
					public void close() throws IOException {
						getInput().close();
						getOutput().close();
					}
				};
				this.poolExecutor.execute(new Command(chnn));
			} catch (Exception e) {
				if (this.transport == null)
					break;
				if (!(this.transport.isClosed()))
					LOG.error(e.getMessage(), e);
			}
		}
		LOG.info("transport closed: " + this.transport);
	}

	protected void accepted(StreamChannel channel) {
		InputStream input = channel.getInput();
		OutputStream output = channel.getOutput();
		String charset = getProperty("encoding");
		try {
			byte[] recvBuf = new byte[this.buffer_capacity];

			int length = read(input, recvBuf);

			byte[] errorBuf = "Unrecognized request".getBytes(charset);
			if (length > 0) {
				LOG.debug("Received Data: "
						+ new String(recvBuf, 0, length, charset));

				String code = getBusinessCode(recvBuf);

				OperationEntry entry = (OperationEntry) this.provideEntries
						.get(code);
				if (entry == null) {
					LOG.debug("Unrecognized request, trancode is: " + code);
					write(output, errorBuf, errorBuf.length);
					return;
				}
				if (entry.getSchema() == null) {
					errorBuf = "Schema not found".getBytes(charset);
					write(output, errorBuf, errorBuf.length);
					return;
				}

				Element requestEl = raw2xml(recvBuf, length, entry.getSchema());

				String serial = getSerialNo(requestEl,
						getProperty("serial-path"));

				if (entry.getData() == null) {
					errorBuf = "Reponse Data not found".getBytes(charset);
					write(output, errorBuf, errorBuf.length);
					return;
				}
				Element rootEl = entry.getData().getRootElement();
				String index = getProperty("selector");
				if (index.equals("random()")) {
					Random rand = new Random();
					index = String.valueOf(rand.nextInt(rootEl.getChildren()
							.size()) + 1);
					System.out.println("random index: " + index);
				}
				Element responseEl = (Element) XPath.selectSingleNode(rootEl,
						"response[" + index + "]");

				byte[] sendBuf = new byte[this.buffer_capacity];
				int len = xml2raw(responseEl, entry.getSchema(), sendBuf);

				LOG.debug("Send Data: " + new String(sendBuf, 0, len, charset));

				write(output, sendBuf, len);
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			try {
				String error = e.getMessage();
				if (error == null)
					error = e.toString();
				byte[] errorBuf = error.getBytes(charset);
				write(output, errorBuf, errorBuf.length);
			} catch (IOException ex) {
				LOG.error("写响应失败：" + e.getMessage());
			}
		} finally {
			try {
				channel.close();
			} catch (IOException e) {
				LOG.error("通道关闭失败：" + e.getMessage());
			}
		}
	}

	protected String getSerialNo(Element requestEl, String xpath)
			throws Exception {
		Element serialEl = (Element) XPath.selectSingleNode(requestEl, xpath);
		return ((serialEl != null) ? serialEl.getText() : null);
	}

	protected int xml2raw(Element dataEl, XmlSchema schema, byte[] result)
			throws Exception {
		dataEl.setName("response");
		return super.xml2raw(dataEl, schema, result);
	}

	protected Element raw2xml(byte[] buffer, int length, XmlSchema schema)
			throws Exception {
		return super.raw2xml(buffer, length, schema, "request");
	}

	protected Decoder getDecoder() throws Exception {
		return ((Decoder) this.decodeClass.newInstance());
	}

	protected Encoder getEncoder() throws Exception {
		return ((Encoder) this.encodeClass.newInstance());
	}

	protected int read(InputStream input, byte[] bytes) throws IOException {
		int count = 0;
		int p;
		int l;
		if (this.headEncoding.equals("2")) {
			input.skip(this.recvOffset);
			ByteBuffer headBuf = ByteBuffer.allocate(this.recvHeadLen);
			headBuf.order(this.byteOrder);
			switch (headBuf.capacity()) {
			case 2:
				input.read(headBuf.array());
				count = headBuf.getShort();
				break;
			case 4:
				input.read(headBuf.array());
				count = headBuf.getInt();
				break;
			default:
				throw new IOException("非法的消息首部长度");
			}
			if (this.lengthIncludeHead)
				count -= this.recvHeadLen;
			for (p = 0; (l = input.read(bytes, p, count - p)) < count - p;) {
				p += l;
			}
		} else {
			if (this.headEncoding.equals("10")) {
				input.skip(this.recvOffset);
				byte[] headBuf = new byte[this.recvHeadLen];
				input.read(headBuf);
				count = Integer.parseInt(new String(headBuf));
				if (this.lengthIncludeHead)
					count -= this.recvHeadLen;
				for (p = 0; (l = input.read(bytes, p, count - p)) < count - p;) {
					p += l;
				}
			} else {
				if (this.headEncoding.equals("16")) {
					input.skip(this.recvOffset);
					byte[] headBuf = new byte[this.recvHeadLen];
					input.read(headBuf);
					count = Integer.parseInt(new String(headBuf), 16);
					if (this.lengthIncludeHead)
						count -= this.recvHeadLen;
					for (p = 0; (l = input.read(bytes, p, count - p)) < count
							- p;) {
						p += l;
					}
				} else {
					if (this.headEncoding.equals("20")) {
						input.skip(this.recvOffset);
						byte[] headBuf = new byte[this.recvHeadLen];
						input.read(headBuf);
						count = Integer.parseInt(bcd2String(headBuf));
						if (this.lengthIncludeHead)
							count -= this.recvHeadLen;
						for (p = 0; (l = input.read(bytes, p, count - p)) < count
								- p;) {
							p += l;
						}
					} else {
						p = 0;
						while ((l = input.read(bytes, p, 1024)) != -1) {
							p += l;
						}
						count = p;
					}
				}
			}
		}
		return count;
	}

	protected void write(OutputStream output, byte[] bytes, int length)
			throws IOException {
		output.write(new byte[this.sendOffset]);

		int count = length;
		if (this.lengthIncludeHead)
			count -= this.sendHeadLen;

		if (this.headEncoding.equals("2")) {
			ByteBuffer headBuf = ByteBuffer.allocate(this.sendHeadLen);
			headBuf.order(this.byteOrder);
			switch (headBuf.capacity()) {
			case 2:
				headBuf.putShort(new Integer(count).shortValue());
				output.write(headBuf.array());
				break;
			case 4:
				headBuf.putInt(count);
				output.write(headBuf.array());
				break;
			default:
				throw new IOException("非法的消息首部长度");
			}
		} else if (this.headEncoding.equals("10")) {
			String head = String.format("%0" + this.sendHeadLen + "d",
					new Object[] { Integer.valueOf(count) });
			output.write(head.getBytes());
		} else if (this.headEncoding.equals("16")) {
			String head = String.format("%0" + this.sendHeadLen + "x",
					new Object[] { Integer.valueOf(count) });
			output.write(head.getBytes());
		} else if (this.headEncoding.equals("20")) {
			String head = String.format("%0" + (this.sendHeadLen * 2) + "d",
					new Object[] { Integer.valueOf(count) });
			output.write(string2Bcd(head));
		}

		output.write(bytes, 0, length);
		output.flush();
	}

	protected String getBusinessCode(byte[] bytes) {
		int start = Integer.parseInt(getProperty("code_begin"));
		int end = Integer.parseInt(getProperty("code_end"));
		return new String(bytes, start, end - start);
	}

	protected void dispose() {
		this.provideEntries.clear();
		this.transport = null;
		super.dispose();
	}

	public void init() throws Exception {
		super.init();
		String hostname = String.valueOf(getProperty("hostname"));
		int port = 0;
		try {
			port = Integer.parseInt(getProperty("port"));
		} catch (NumberFormatException e) {
			throw new Exception("port must be a valid number");
		}
		this.socketAddress = new InetSocketAddress(hostname, port);
	}

	public void shutDown() throws Exception {
		stop();
		if (this.poolExecutor.getActiveCount() > 0)
			this.poolExecutor.shutdown();
		else
			this.poolExecutor.shutdownNow();
		dispose();
	}

	public void start() throws Exception {
		this.transport = new ServerSocket();
		this.transport.bind(this.socketAddress);
		this.poolExecutor.prestartCoreThread();
		this.acceptThread = new Thread(this);
		this.acceptThread.start();
		super.start();
	}

	public void stop() throws Exception {
		if (this.acceptThread != null) {
			try {
				this.acceptThread.interrupt();
				if (this.poolExecutor.getActiveCount() > 0)
					this.poolExecutor.awaitTermination(5000L,
							TimeUnit.MILLISECONDS);
				else
					this.poolExecutor.awaitTermination(0L,
							TimeUnit.MILLISECONDS);
				LOG.debug("PoolExecutor Terminated!");
			} catch (InterruptedException e) {
			} finally {
				this.transport.close();
			}
		}
		super.stop();
	}

	private class Command implements Runnable {
		StreamChannel chnn;

		Command(StreamChannel paramStreamChannel) {
			this.chnn = paramStreamChannel;
		}

		public void run() {
			TcpServer.this.accepted(this.chnn);
		}
	}
}