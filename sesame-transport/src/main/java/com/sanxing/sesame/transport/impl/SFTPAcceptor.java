package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.util.SesameSFTPClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFTPAcceptor extends TaskTransport {
	private static Logger LOG = LoggerFactory.getLogger(SFTPAcceptor.class);

	public void reply(MessageContext context) throws IOException {
		String path = context.getPath();
		Map props = (Map) this.bindingPropsCache.get(path);

		BinaryResult result = (BinaryResult) context.getResult();
		byte[] responseByte = result.getBytes();
		if (responseByte != null) {
			SesameSFTPClient sftpClient = new SesameSFTPClient();
			try {
				sftpClient.setURI(this.uri);
				sftpClient.setProperties(this.properties);
				sftpClient.setBindingProperties(props);
				sftpClient.init();
				LOG.debug("reply message is:" + new String(responseByte));

				sftpClient.putFile(context.getAction() + "_response",
						responseByte);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					sftpClient.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void close() throws IOException {
		try {
			this.scheduler.shutdown();
			this.workExecutor.shutdown();
			this.active = false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public String getCharacterEncoding() {
		return "utf-8";
	}

	public String getDescription() {
		return "SFTP Transport";
	}

	public void open() throws IOException {
		try {
			this.active = true;

			this.scheduler = new SesameScheduler();
			this.scheduler.init();
			this.scheduler.start();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public void executeTask(Map<?, ?> properties) throws Exception {
		SesameSFTPClient sftpClient = new SesameSFTPClient();
		try {
			sftpClient.setURI(this.uri);
			sftpClient.setProperties(this.properties);
			sftpClient.setBindingProperties(properties);
			sftpClient.init();

			FileObject[] files = sftpClient.getFiles();
			for (FileObject file : files) {
				if ((file.getType() != FileType.FILE)
						|| (!(sftpClient.moveFile(file))))
					continue;
				InputStream in = null;
				String filename = file.getName().getBaseName();
				LOG.debug("process file:" + filename);
				in = sftpClient.processOneFile(filename);
				accept(in, filename, (String) properties.get("jobName"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				sftpClient.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}