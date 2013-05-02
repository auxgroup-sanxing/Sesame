package com.sanxing.sesame.transport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class SesameFTP {
	private FTPClient ftpClient;
	public static final int BINARY_FILE_TYPE = 2;
	public static final int ASCII_FILE_TYPE = 0;

	public void connectServer(URL url) throws SocketException, IOException {
		String server = url.getHost();
		int port = url.getPort();

		String userInfo = url.getUserInfo();
		int index = (userInfo != null) ? userInfo.indexOf(58) : 0;
		String user = (index > 0) ? userInfo.substring(0, index) : userInfo;
		String password = (index > 0) ? user.substring(index + 1) : "";
		String path = url.getPath();
		connectServer(server, port, user, password, path);
	}

	public void connectServer(String server, int port, String user,
			String password, String path) throws SocketException, IOException {
		this.ftpClient = new FTPClient();
		this.ftpClient.connect(server, port);
		System.out.println("Connected to " + server + ".");
		System.out.println(this.ftpClient.getReplyCode());
		this.ftpClient.login(user, password);

		if (path.length() != 0)
			this.ftpClient.changeWorkingDirectory(path);
	}

	public void setFileType(int fileType) throws IOException {
		this.ftpClient.setFileType(fileType);
	}

	public void closeServer() throws IOException {
		if (this.ftpClient.isConnected())
			this.ftpClient.disconnect();
	}

	public boolean changeDirectory(String path) throws IOException {
		return this.ftpClient.changeWorkingDirectory(path);
	}

	public boolean createDirectory(String pathname) throws IOException {
		return this.ftpClient.makeDirectory(pathname);
	}

	public boolean mkdirs(String pathname) throws IOException {
		if (this.ftpClient.makeDirectory(pathname)) {
			return true;
		}

		int index = pathname.lastIndexOf(47);
		if (index > 0) {
			if (mkdirs(pathname.substring(0, index))) {
				return this.ftpClient.makeDirectory(pathname);
			}

			return false;
		}

		return false;
	}

	public boolean removeDirectory(String path) throws IOException {
		return this.ftpClient.removeDirectory(path);
	}

	public boolean removeDirectory(String path, boolean isAll)
			throws IOException {
		if (!(isAll)) {
			return removeDirectory(path);
		}

		FTPFile[] ftpFileArr = this.ftpClient.listFiles(path);
		if ((ftpFileArr == null) || (ftpFileArr.length == 0)) {
			return removeDirectory(path);
		}

		for (FTPFile ftpFile : ftpFileArr) {
			String name = ftpFile.getName();
			if (ftpFile.isDirectory()) {
				System.out.println("* [sD]Delete subPath [" + path + "/" + name
						+ "]");
				removeDirectory(path + "/" + name, true);
			} else if (ftpFile.isFile()) {
				System.out.println("* [sF]Delete file [" + path + "/" + name
						+ "]");
				deleteFile(path + "/" + name);
			} else {
				if (ftpFile.isSymbolicLink()) {
					continue;
				}
				ftpFile.isUnknown();
			}
		}

		return this.ftpClient.removeDirectory(path);
	}

	public boolean existDirectory(String path) throws IOException {
		boolean flag = false;
		FTPFile[] ftpFileArr = this.ftpClient.listFiles(path);
		for (FTPFile ftpFile : ftpFileArr) {
			if ((ftpFile.isDirectory())
					&& (ftpFile.getName().equalsIgnoreCase(path))) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public List<String> getFileList(String path) throws IOException {
		FTPFile[] ftpFiles = this.ftpClient.listFiles(path);

		List retList = new ArrayList();
		if ((ftpFiles == null) || (ftpFiles.length == 0)) {
			return retList;
		}
		for (FTPFile ftpFile : ftpFiles) {
			if (ftpFile.isFile()) {
				retList.add(ftpFile.getName());
			}
		}
		return retList;
	}

	public boolean deleteFile(String pathName) throws IOException {
		return this.ftpClient.deleteFile(pathName);
	}

	public boolean uploadFile(String fileName, String newName)
			throws IOException {
		boolean flag = false;
		InputStream iStream = null;
		try {
			iStream = new FileInputStream(fileName);
			flag = this.ftpClient.storeFile(newName, iStream);
		} catch (IOException e) {
			flag = false;
			return flag;
		} finally {
			if (iStream != null) {
				iStream.close();
			}
		}
		return flag;
	}

	public boolean uploadFile(String fileName) throws IOException {
		return uploadFile(fileName, fileName);
	}

	public boolean uploadFile(InputStream iStream, String newName)
			throws IOException {
		boolean flag = false;
		try {
			flag = this.ftpClient.storeFile(newName, iStream);
		} catch (IOException e) {
			flag = false;
			return flag;
		} finally {
			if (iStream != null) {
				iStream.close();
			}
		}
		return flag;
	}

	public boolean download(String remoteFileName, String localFileName)
			throws IOException {
		boolean flag = false;
		File outfile = new File(localFileName);
		OutputStream oStream = null;
		try {
			oStream = new FileOutputStream(outfile);
			flag = this.ftpClient.retrieveFile(remoteFileName, oStream);
		} catch (IOException e) {
			flag = false;
			return flag;
		} finally {
			if(oStream != null)
				oStream.close();
		}
		return flag;
	}

	public InputStream downFile(String sourceFileName) throws IOException {
		return this.ftpClient.retrieveFileStream(sourceFileName);
	}
}