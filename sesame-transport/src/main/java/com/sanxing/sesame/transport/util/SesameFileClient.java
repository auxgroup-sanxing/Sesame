package com.sanxing.sesame.transport.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

public class SesameFileClient {
	String getFilePath = "";
	String bakFilePath = "";
	String putFilePath = "";

	public File[] getFiles() throws Exception {
		File getDirectory = new File(this.getFilePath);
		File[] files = getDirectory.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		});
		return files;
	}

	public void setBindingProperties(Map<?, ?> props) {
		this.getFilePath = ((String) props.get("remoteGetPath"));
		this.bakFilePath = ((String) props.get("remoteBakPath"));
		this.putFilePath = ((String) props.get("remotePutPath"));
	}

	public InputStream readOneFile(File file) throws Exception {
		return new FileInputStream(file);
	}

	public InputStream readOneFileWithName(String filename) throws Exception {
		return new FileInputStream(this.bakFilePath + "/" + filename);
	}

	public byte[] getInputByte(InputStream in) throws IOException {
		byte[] result = (byte[]) null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] temp = new byte[1024];
		int len = 0;
		while ((len = in.read(temp)) != -1) {
			in.read(temp);
			output.write(temp, 0, len);
			len = 0;
		}
		in.close();
		result = output.toByteArray();
		output.close();
		return result;
	}

	public void writeOneFile(byte[] resp) throws Exception {
		File respFile = new File(this.putFilePath, "response.txt");
		FileOutputStream out = new FileOutputStream(respFile);

		out.write(resp);

		out.close();
	}

	public void uploadOneFile(byte[] buf, String filename) throws Exception {
		File respFile = new File(this.putFilePath, filename);
		FileOutputStream out = new FileOutputStream(respFile);
		out.write(buf);
		out.close();
	}

	public void copyFile(String src, String des) throws Exception {
		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(des);

		FileChannel fcIn = in.getChannel();
		FileChannel fcOut = out.getChannel();

		ByteBuffer buffer = ByteBuffer.allocate(1024);

		while (fcIn.read(buffer) != -1) {
			buffer.flip();
			fcOut.write(buffer);
			buffer.clear();
		}
		fcIn.close();
		fcOut.close();
		in.close();
		out.close();
	}

	public void copyFile(File src, File des) throws Exception {
		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(des);

		FileChannel fcIn = in.getChannel();
		FileChannel fcOut = out.getChannel();

		ByteBuffer buffer = ByteBuffer.allocate(1024);

		while (fcIn.read(buffer) != -1) {
			buffer.flip();
			fcOut.write(buffer);
			buffer.clear();
		}
		fcIn.close();
		fcOut.close();
		in.close();
		out.close();
	}

	public boolean bakFile(File file) throws Exception {
		File bakFile = new File(this.bakFilePath + "/" + file.getName());
		copyFile(file, bakFile);
		return deleteFile(file);
	}

	public boolean bakFile(String filename) throws Exception {
		copyFile(this.getFilePath + "/" + filename, this.bakFilePath + "/"
				+ filename);
		return deleteFileWithName(this.getFilePath + "/" + filename);
	}

	public boolean deleteFile(File file) throws Exception {
		return file.delete();
	}

	public boolean deleteFileWithName(String filename) throws Exception {
		File file = new File(filename);
		return file.delete();
	}

	public void setProperties(Map<?, ?> props) {
		setBindingProperties(props);
	}
}