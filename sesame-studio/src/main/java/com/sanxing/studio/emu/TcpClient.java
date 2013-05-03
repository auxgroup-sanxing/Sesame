package com.sanxing.studio.emu;

import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

public class TcpClient extends BindingUnit implements Client {
	private static final int DEFAULT_BUFFER_CAPACITY = 8192;
	protected static final Logger LOG = LoggerFactory.getLogger(TcpClient.class);
	private static final int DEFAULT_READ_LENGTH = 1024;
	private Map<String, OperationEntry> consumeEntries = new Hashtable();
	private Thread sendThread;
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

	public TcpClient(Map<?, ?> properties, File serviceUnitRoot)
			throws Exception {
		super(properties, serviceUnitRoot);
		this.poolExecutor = new ThreadPoolExecutor(10, 100, 10000L,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue(100));
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

		String uri = definition.getTargetNamespace();
		for (Iterator iter = portTypes.values().iterator(); iter.hasNext();) {
			PortType intfEl = (PortType) iter.next();
			List opers = intfEl.getOperations();
			for (Iterator it = opers.iterator(); it.hasNext();) {
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
				this.consumeEntries.put(code, entry);
			}
		}
	}

	public void send(byte[] buffer, InetSocketAddress address) throws Exception {
		Socket socket = new Socket(address.getHostName(), address.getPort());
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();
		try {
			output.write(buffer);

			byte[] result = new byte[8192];
			int len = read(input, result);
			if (len > 0)
				System.out.println("received: "
						+ new String(result, 0, len, getProperty("encoding")));
		} finally {
			input.close();
			output.close();
		}
	}

	protected int read(InputStream input, byte[] bytes) throws IOException {
		int count = 0;
		int p;
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
			int l;
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
				int l;
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
					int l;
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
						int l;
						for (p = 0; (l = input.read(bytes, p, count - p)) < count
								- p;) {
							p += l;
						}
					} else {
						p = 0;
						int l;
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

	protected void dispose() {
		this.consumeEntries.clear();
		super.dispose();
	}

	protected Decoder getDecoder() throws Exception {
		return ((Decoder) this.decodeClass.newInstance());
	}

	protected Encoder getEncoder() throws Exception {
		return ((Encoder) this.encodeClass.newInstance());
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

	public void start() throws Exception {
		this.poolExecutor.prestartCoreThread();
		this.sendThread = new Thread(this);
		this.sendThread.start();
		super.start();
	}

	public void stop() throws Exception {
		super.stop();
		if (this.sendThread == null)
			return;
		try {
			this.sendThread.interrupt();
			if (this.poolExecutor.getActiveCount() > 0)
				this.poolExecutor
						.awaitTermination(5000L, TimeUnit.MILLISECONDS);
			else
				this.poolExecutor.awaitTermination(0L, TimeUnit.MILLISECONDS);
			LOG.debug("PoolExecutor Terminated!");
		} catch (InterruptedException e) {
			throw new Exception(e);
		}
	}

	public void shutDown() throws Exception {
		stop();
		if (this.poolExecutor.getActiveCount() > 0)
			this.poolExecutor.shutdown();
		else
			this.poolExecutor.shutdownNow();
		dispose();
	}

	public void run() {
		int timeout = getProperty("timeout", 30) * 1000;
		int interval = getProperty("interval", 1) * 1000;
		while (getCurrentState() == 1)
			try {
				Socket socket = new Socket();
				socket.connect(this.socketAddress, timeout);
				LOG.debug("Socket connected to " + socket.getInetAddress()
						+ ":" + socket.getPort());

				StreamChannel chnn = new StreamChannel(socket.getInputStream(),
						socket.getOutputStream()) {
					public void close() throws IOException {
						getInput().close();
						getOutput().close();
					}
				};
				this.poolExecutor.execute(new Command(chnn));
				Thread.sleep(interval);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
	}

	public void connected(StreamChannel channel) {
		Random rand = new Random();
		Object[] entries = this.consumeEntries.keySet().toArray();
		try {
			String code;
			OperationEntry entry;
			do {
				int index = rand.nextInt(entries.length);
				code = (String) entries[index];
				entry = (OperationEntry) this.consumeEntries.get(code);
			} while (entry.getData() == null);

			Element rootEl = entry.getData().getRootElement();
			String selector = getProperty("selector");
			if (selector.equals("random()")) {
				Random r = new Random();
				selector = String.valueOf(r
						.nextInt(rootEl.getChildren().size()) + 1);
			}
			Element dataEl = (Element) XPath.selectSingleNode(rootEl,
					"request[" + selector + "]");
			sendRecv(channel, code, dataEl);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public Element sendRecv(StreamChannel channel, String code, Element dataEl)
			throws IOException {
		InputStream input = channel.getInput();
		OutputStream output = channel.getOutput();
		String charset = getProperty("encoding");
		try {
			OperationEntry entry = (OperationEntry) this.consumeEntries
					.get(code);
			if (entry.getSchema() == null) {
				LOG.debug(entry + ": Schema not found");
				return null;
			}
			byte[] sendBuf = new byte[this.buffer_capacity];
			int len = xml2raw(dataEl, entry.getSchema(), sendBuf);

			LOG.debug("Send Data: " + new String(sendBuf, 0, len, charset));

			write(output, sendBuf, len);

			byte[] recvBuf = new byte[this.buffer_capacity];
			int length = read(input, recvBuf);
			String stringBuf;
			if (length > 0) {
				stringBuf = new String(recvBuf, 0, length, charset);
				LOG.debug("Received Data: " + stringBuf);
				return raw2xml(recvBuf, length, entry.getSchema());
			}

			throw new IOException("Received Data is blank");
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
		} finally {
			channel.close();
		}
		return null;
	}

	protected int xml2raw(Element dataEl, XmlSchema schema, byte[] result)
			throws Exception {
		dataEl.setName("request");
		return super.xml2raw(dataEl, schema, result);
	}

	protected Element raw2xml(byte[] buffer, int length, XmlSchema schema)
			throws Exception {
		return super.raw2xml(buffer, length, schema, "response");
	}

	public Element send(String code, Element data, int timeout)
			throws IOException {
		LOG.debug("Connect " + this.socketAddress);
		Socket socket = new Socket();
		socket.connect(this.socketAddress, timeout);
		LOG.debug("Socket connected to " + socket.getInetAddress() + ":"
				+ socket.getPort());

		StreamChannel chnn = new StreamChannel(socket.getInputStream(),
				socket.getOutputStream()) {
			public void close() throws IOException {
				getInput().close();
				getOutput().close();
			}
		};
		return sendRecv(chnn, code, data);
	}

	private class Command implements Runnable {
		StreamChannel chnn;

		Command(StreamChannel paramStreamChannel) {
			this.chnn = paramStreamChannel;
		}

		public void run() {
			TcpClient.this.connected(this.chnn);
		}
	}
}