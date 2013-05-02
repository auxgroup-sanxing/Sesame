package com.sanxing.sesame.jmx.mbean.managed;

import com.sanxing.sesame.jmx.FilePackage;
import com.sanxing.sesame.jmx.mbean.admin.FileServerMBean;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.util.FileUtil;
import java.io.File;
import java.io.FileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileClient implements FileClientMBean {
	private static Logger LOG = LoggerFactory.getLogger(FileClient.class);

	public String fetchFile(String fileName) {
		int index = fileName.lastIndexOf("\\");
		if (index == -1) {
			index = fileName.lastIndexOf("/");
		}
		if (index == -1) {
			index = 0;
		}
		File homeDir = Platform.getEnv().getHomeDir();
		File file = new File(homeDir, "work/temp/"
				+ fileName.substring((index == 0) ? 0 : index + 1));
		FileUtil.buildDirectory(file.getParentFile());

		if (LOG.isDebugEnabled())
			LOG.debug("Target file name is ---> " + file);
		FilePackage filePackage = new FilePackage(fileName);
		filePackage.setCurrentPackage(1L);
		filePackage.setPageSize(10240L);
		try {
			FileServerMBean fileServer = (FileServerMBean) MBeanHelper
					.getAdminMBean(FileServerMBean.class, "file-server");

			FileOutputStream fos = new FileOutputStream(file);
			try {
				do {
					filePackage = fileServer.transfer(filePackage);
					fos.write(filePackage.getPackageData());
				} while (!(filePackage.isEnd()));
			} finally {
				fos.close();
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return file.getAbsolutePath();
	}

	public String getDescription() {
		return "文件传输客户端";
	}
}