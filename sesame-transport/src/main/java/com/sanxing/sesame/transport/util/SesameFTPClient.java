package com.sanxing.sesame.transport.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameFTPClient {
	private static Logger LOG = LoggerFactory.getLogger(SesameFTPClient.class);

	private FTPClient ftp = new FTPClient();
	private URI uri;
	private String remoteGetPath;
	private String remoteBakPath;
	private String localBak;
	private String localBakPath;
	private String remotePutPath;
	private String ftpHost;
	private int ftpPort = 21;
	private String ftpUser;
	private String ftpPassword;
	private String binary = "true";
	private String passive = "true";
	private String debug = "true";

	private String encoding = "gbk";
	private int timeout = 30;

	public boolean connectAddLogin() throws IOException {
		boolean result = false;
		if (connect()) {
			result = login();
		}
		LOG.debug("login result is:" + result);
		return result;
	}

	public void logoutAddDisconnect() throws IOException {
		logout();
		disconnect();
	}

	private boolean connect() throws IOException {
		return connect(this.ftpHost, this.ftpPort);
	}

	private boolean connect(String hostname, int port) throws IOException {
		boolean result = false;

		this.ftp.setDefaultTimeout(this.timeout * 1000);

		this.ftp.setControlEncoding("gbk");

		this.ftp.connect(hostname, port);
		this.ftp.enterLocalActiveMode();

		int reply = this.ftp.getReplyCode();
		if (!(FTPReply.isPositiveCompletion(reply))) {
			this.ftp.disconnect();
			result = false;
		} else {
			result = true;
		}
		return result;
	}

	public void disconnect() throws IOException {
		if (this.ftp.isConnected())
			this.ftp.disconnect();
	}

	private boolean logout() throws IOException {
		return this.ftp.logout();
	}

	private boolean login() throws IOException {
		boolean result = false;
		if (login(this.ftpUser, this.ftpPassword)) {
			result = true;
			if (this.binary.equals("true"))
				setFileType();
			if (this.passive.equals("true"))
				setLocalModel();
			if (this.debug.equals("true")) {
				setPrintListener();
			}
		}

		return result;
	}

	private boolean login(String username, String password) throws IOException {
		return this.ftp.login(username, password);
	}

	private boolean setFileType() throws IOException {
		return this.ftp.setFileType(2);
	}

	private void setLocalModel() {
		this.ftp.enterLocalPassiveMode();
	}

	private void setPrintListener() {
		this.ftp.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.out)));
	}

	public boolean isConnected() {
		return this.ftp.isConnected();
	}

	private void reconnect() throws IOException {
		if ((isConnected()) || (connectAddLogin()))
			return;
		throw new IOException("can not connect and login to ftp server.");
	}

	public boolean changeWorkingDirectory(String pathname) throws IOException {
		reconnect();
		return this.ftp.changeWorkingDirectory(pathname);
	}

	public boolean changeToParentDirectory() throws IOException {
		reconnect();
		return this.ftp.changeToParentDirectory();
	}

	public boolean makeDirectory(String pathname) throws IOException {
		reconnect();
		return this.ftp.makeDirectory(pathname);
	}

	public String printWorkingDirectory() throws IOException {
		reconnect();
		return this.ftp.printWorkingDirectory();
	}

	public boolean moveFile(String filename) throws IOException {
		reconnect();
		return moveFile(this.remoteGetPath + "/" + filename, this.remoteBakPath
				+ "/" + filename);
	}

	public boolean moveFile(String oldPath, String newPath) throws IOException {
		LOG.debug("....oldPath is:" + oldPath + ",newPath is:" + newPath);
		reconnect();
		return this.ftp.rename(oldPath, newPath);
	}

	public Object[] getFileNames() throws IOException {
		reconnect();
		return getFileNames(this.remoteGetPath);
	}

	public Object[] getFileNames(String remote) throws IOException {
		reconnect();
		LOG.debug(".................get files path is:" + remote);
		List nameList = new ArrayList();

		FTPFile[] files = this.ftp.listFiles(remote);
		for (FTPFile file : files) {
			if (file.isFile())
				nameList.add(file.getName());
		}
		return nameList.toArray();
	}

	public byte[] getInputByte(InputStream in) throws IOException {
		reconnect();
		byte[] result = (byte[]) null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] temp = new byte[1024];
		int len = 0;
		while ((len = in.read(temp)) != -1) {
			output.write(temp, 0, len);
			len = 0;
		}
		in.close();
		if (!(this.ftp.completePendingCommand())) {
			this.ftp.logout();
			this.ftp.disconnect();
			throw new IOException("get file from ftp server error.");
		}

		result = output.toByteArray();
		output.close();

		return result;
	}

	public InputStream processOneFile(String filename) throws IOException {
		reconnect();
		InputStream in;
		if (this.localBak.equals("true")) {
			in = backupAndProcessOneFile(this.remoteBakPath + "/" + filename,
					this.localBakPath + "/" + filename);
		} else {
			in = processOneFileByStream(this.remoteBakPath + "/" + filename);
		}
		return in;
	}

	private InputStream processOneFileByStream(String filePath)
			throws IOException {
		reconnect();
		InputStream in = this.ftp.retrieveFileStream(filePath);
		return in;
	}

	public boolean mkdirs(String pathname) throws IOException {
		if (this.ftp.makeDirectory(pathname)) {
			return true;
		}

		int index = pathname.lastIndexOf(47);
		if (index > 0) {
			if (mkdirs(pathname.substring(0, index))) {
				return this.ftp.makeDirectory(pathname);
			}

			return false;
		}

		return false;
	}

	public boolean uploadFile(String pathname, InputStream in)
			throws IOException {
		reconnect();
		int index = pathname.lastIndexOf(47);
		if (index > 0) {
			mkdirs(pathname.substring(0, index));
		}

		this.ftp.storeFile(((this.remotePutPath != null) ? this.remotePutPath
				: "") + pathname, in);

		String status = this.ftp.getStatus(pathname);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Uploaded file status: " + status);
		}

		return (status != null);
	}

	private InputStream backupAndProcessOneFile(String remoteFilePath,
			String localFilePath) throws IOException {
		reconnect();
		InputStream in = null;
		FileOutputStream out = new FileOutputStream(localFilePath);
		if (this.ftp.retrieveFile(remoteFilePath, out)) {
			out.close();
			in = new FileInputStream(localFilePath);
		} else {
			out.close();
			throw new IOException("backup file  error!");
		}
		return in;
	}

	public boolean completePendingCommand() throws IOException {
		reconnect();
		return this.ftp.completePendingCommand();
	}

	public boolean deleteFile(String pathname) throws IOException {
		reconnect();
		return this.ftp.deleteFile(pathname);
	}

	public boolean removeDirectory(String pathname) throws IOException {
		reconnect();
		return this.ftp.removeDirectory(pathname);
	}

	public void setConnectorProperties(Map<?, ?> props) {
		setProperties(props);
		setBindingProperties(props);
	}

	public void setProperties(Map<?, ?> props) {
		this.ftpHost = this.uri.getHost();
		this.ftpPort = this.uri.getPort();
		this.ftpUser = ((String) props.get("ftpUser"));
		this.ftpPassword = ((String) props.get("ftpPassword"));
		this.binary = ((String) props.get("binary"));
		this.passive = ((String) props.get("passive"));
		this.debug = ((String) props.get("debug"));
		if ((props.get("timeout") != null)
				&& (((String) props.get("timeout")).length() > 0))
			this.timeout = Integer.parseInt((String) props.get("timeout"));
	}

	public void setBindingProperties(Map<?, ?> props) {
		if (props != null) {
			this.remoteGetPath = ((String) props.get("remoteGetPath"));
			this.localBak = ((String) props.get("localBak"));
			this.remoteBakPath = ((String) props.get("remoteBakPath"));
			this.remotePutPath = ((String) props.get("remotePutPath"));
			this.localBakPath = ((String) props.get("localBakPath"));
		}
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public FTPClientConfig getFtpConfig() {
		FTPClientConfig config = new FTPClientConfig("WINDOWS");
		return config;
	}

	public static void main(String[] args) {
		try {
			SesameFTPClient client = new SesameFTPClient();
			client.ftpHost = "10.14.3.73";
			client.ftpPort = 21;
			client.ftpUser = "sesame";
			client.ftpPassword = "password";

			client.remoteGetPath = "ftptest/download";

			client.localBak = "false";

			client.remoteBakPath = "ftptest/upload";
			client.processOneFile("ftpget");

			client.logoutAddDisconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}