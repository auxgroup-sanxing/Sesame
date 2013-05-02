package com.sanxing.adp.util;

import java.io.File;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	private static Logger LOG = LoggerFactory.getLogger(FileUtil.class);

	public static String getTargetDir(String url) {
		String result;
		try {
			if (url.startsWith("file")) {
				URL u = new URL(url);
				File file = new File(u.getFile());
				return new File(file.getAbsolutePath()).getParent();
			}
			File file = new File(url);
			return file.getParentFile().getAbsolutePath();
		} catch (Exception e) {
			LOG.error("getTargetDir err", e);
			String[] temp = StringUtils.split(url, File.separator);
			result = "";
			for (int i = 0; i < temp.length - 1; ++i) {
				result = result + temp[i];
			}
		}
		return result;
	}
}