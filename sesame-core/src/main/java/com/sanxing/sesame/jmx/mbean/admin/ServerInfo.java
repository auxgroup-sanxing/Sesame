package com.sanxing.sesame.jmx.mbean.admin;

import com.sanxing.sesame.executors.impl.ExecutorConfig;
import com.sanxing.sesame.core.Detector;
import com.sanxing.sesame.core.jdbc.DataSourceInfo;
import com.sanxing.sesame.core.jms.JMSResourceEntry;
import com.sanxing.sesame.core.jms.JMSServiceInfo;
import com.sanxing.sesame.util.GetterUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ServerInfo implements Serializable {
	private static final long serialVersionUID = -1926539493979743863L;
	private int serverState = 0;
	private String serverName;
	private String IP;
	private boolean admin;
	private long lastHearBeatTimestamp;
	private Element xmlElement;
	private JMSServiceInfo jmsServiceInfo;
	private List<DataSourceInfo> datasourceInfos = new LinkedList();

	private List<ContainerInfo> containerInfos = new LinkedList();

	private Map<String, ExecutorConfig> executorInfos = new HashMap();

	private static Logger LOG = LoggerFactory.getLogger(ServerInfo.class);

	public List<ContainerInfo> getContainerInfos() {
		return this.containerInfos;
	}

	public static ServerInfo fromFile(File serverDir, String serverName)
			throws FileNotFoundException {
		try {
			File file = new File(serverDir, "conf/" + serverName + ".xml");

			if (file.exists()) {
				Document doc = new SAXBuilder().build(file);
				Element root = doc.getRootElement();
				return fromElement(root);
			}

			InputStream input = ServerInfo.class.getClassLoader()
					.getResourceAsStream("admin.xml");
			Document doc = new SAXBuilder().build(input);
			Element root = doc.getRootElement();

			XMLOutputter output = new XMLOutputter();
			output.setFormat(Format.getPrettyFormat());
			output.output(doc, new FileOutputStream(file));

			return fromElement(root);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
		}
		return null;
	}

	public static ServerInfo fromElement(Element serverElement) {
		try {
			Element root = serverElement;

			ServerInfo server = new ServerInfo();
			server.xmlElement = root;
			server.serverName = root.getChildText("server-name");
			server.IP = root.getChildText("IP");
			server.admin = GetterUtil.getBoolean(root.getChildText("admin"),
					true);

			parseJMSResource(root, server);
			parseJDBCResource(root, server);

			parseExecutor(root, server);

			if (root.getChild("containers") != null) {
				List containerEles = root.getChild("containers").getChildren(
						"container");
				for (Iterator localIterator = containerEles.iterator(); localIterator
						.hasNext();) {
					Object containerEle = localIterator.next();
					Element containerElement = (Element) containerEle;
					ContainerInfo container = new ContainerInfo();
					container.setContainerClazz(containerElement
							.getChildText("class"));
					container.setName(containerElement.getChildText("name"));
					container.setCotnainerParams(containerElement
							.getChild("params"));
					server.containerInfos.add(container);
				}
			}

			return server;
		} catch (Exception e) {
		}
		return null;
	}

	private static void parseExecutor(Element root, ServerInfo server) {
		Element pools = root.getChild("thread-pools");
		if (pools == null) {
			return;
		}
		List poolEles = pools.getChildren();
		for (Iterator localIterator = poolEles.iterator(); localIterator
				.hasNext();) {
			Object obj = localIterator.next();
			ExecutorConfig config = new ExecutorConfig();
			Element poolEle = (Element) obj;
			String id = poolEle.getAttributeValue("id");
			config.setCorePoolSize(Integer.parseInt(poolEle.getChildText(
					"core-pool-size").trim()));
			config.setKeepAliveTime(Integer.parseInt(poolEle.getChildText(
					"keep-alive-time").trim()));
			config.setMaximumPoolSize(Integer.parseInt(poolEle.getChildText(
					"max-pool-size").trim()));
			config.setQueueSize(Integer.parseInt(poolEle.getChildText(
					"queue-size").trim()));
			config.setShutdownDelay(Integer.parseInt(poolEle.getChildText(
					"shutdown-delay").trim()));

			server.executorInfos.put(id, config);
		}
	}

	public static void writeToFile(File serverDir, ServerInfo server)
			throws IOException {
		XMLOutputter output = new XMLOutputter();
		output.setFormat(Format.getPrettyFormat());
		File file = new File(serverDir, "conf/" + server.getServerName()
				+ ".xml");
		output.output(server.xmlElement, new FileWriter(file));
	}

	private static void parseJMSResource(Element root, ServerInfo server) {
		Element eleJMS = root.getChild("jms");

		JMSServiceInfo jmsInfo = new JMSServiceInfo();
		server.setJmsServiceInfo(jmsInfo);
		jmsInfo.setServerName(server.serverName);

		jmsInfo.setAppInfo(eleJMS.getChild("app-info"));
		List entries = eleJMS.getChildren("entry");
		for (int i = 0; i < entries.size(); ++i) {
			Element entry = (Element) entries.get(i);
			JMSResourceEntry jmsEntry = new JMSResourceEntry();
			jmsEntry.setAppInfo(entry.getChild("app-info"));
			jmsEntry.setJndiName(entry.getChildText("jndi-name"));
			jmsEntry.setType(entry.getChildText("type"));
			jmsInfo.getEntries().add(jmsEntry);
		}
	}

	private static void parseJDBCResource(Element root, ServerInfo server) {
		Element jdbcEl = root.getChild("jdbc");
		if (jdbcEl == null) {
			return;
		}
		String transactionManager = root.getChildText("transaction-manager");
		int tranManagerType = TransactionManagerType.NTM;

		if (transactionManager == null)
			tranManagerType = TransactionManagerType.NTM;
		else if (transactionManager.equalsIgnoreCase("STM"))
			tranManagerType = TransactionManagerType.STM;
		else if (transactionManager.equalsIgnoreCase("BTM")) {
			tranManagerType = TransactionManagerType.BTM;
		}

		if (Detector.isInContainer().booleanValue()) {
			tranManagerType = TransactionManagerType.J2EE;
		}

		List dataSources = jdbcEl.getChildren("datasource");
		int stmNumber = 0;
		for (int i = 0; i < dataSources.size(); ++i) {
			Element dsEle = (Element) dataSources.get(i);
			DataSourceInfo dsInfo = new DataSourceInfo();
			dsInfo.setAppInfo(dsEle.getChild("app-info"));
			dsInfo.setJndiName(dsEle.getChildText("jndi-name"));
			LOG.debug("jndi-name: " + dsEle.getChildText("jndi-name"));
			boolean isTransaction = GetterUtil.getBoolean(
					dsEle.getChildText("transaction"), false);
			LOG.debug("transactionManager: " + transactionManager
					+ " isTransaction:" + isTransaction);
			dsInfo.setTransactionManager(tranManagerType);
			if (!(isTransaction)) {
				dsInfo.setTransactionManager(0);
			}

			if ((tranManagerType == 1) && (isTransaction)) {
				if (stmNumber != 0)
					continue;
				++stmNumber;
			}

			server.datasourceInfos.add(dsInfo);
		}
	}

	public JMSServiceInfo getJmsServiceInfo() {
		return this.jmsServiceInfo;
	}

	public void setJmsServiceInfo(JMSServiceInfo jmsServiceInfo) {
		this.jmsServiceInfo = jmsServiceInfo;
	}

	public List<DataSourceInfo> getDatasourceInfos() {
		return this.datasourceInfos;
	}

	public int getServerState() {
		return this.serverState;
	}

	public void setServerState(int serverState) {
		this.serverState = serverState;
	}

	public Map<String, ExecutorConfig> getExecutorInfos() {
		return this.executorInfos;
	}

	public void setExecutorInfos(Map<String, ExecutorConfig> executorInfos) {
		this.executorInfos = executorInfos;
	}

	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getIP() {
		return this.IP;
	}

	public void setIP(String iP) {
		this.IP = iP;
	}

	public boolean isAdmin() {
		return this.admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public long getLastHearBeatTimestamp() {
		return this.lastHearBeatTimestamp;
	}

	public void setLastHearBeatTimestamp(long lastHearBeatTimestamp) {
		this.lastHearBeatTimestamp = lastHearBeatTimestamp;
	}

	public String toString() {
		String temp = "--------------------------------------\n";
		temp = temp + "ServerInfo [severName=" + this.serverName
				+ ",serverState=" + this.serverState + ",IP=" + this.IP
				+ ", admin=" + this.admin + ", datasourceInfos=\n";
		for (DataSourceInfo ds : this.datasourceInfos) {
			temp = temp + ds.toString() + "\n";
		}
		temp = temp + "--------jms-info=--------------------\n";
		temp = temp + this.jmsServiceInfo;
		temp = temp + "-----------------------------------\n";
		return temp;
	}
}