package com.sanxing.studio.deploy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileCopy {
	FileInputStream FIS;
	FileOutputStream FOS;

	public boolean copyFile(String src, String des) {
		try {
			this.FIS = new FileInputStream(src);
			this.FOS = new FileOutputStream(des);
			byte[] bt = new byte[4096];
			int readNum = 0;
			while ((readNum = this.FIS.read(bt)) != -1) {
				this.FOS.write(bt, 0, readNum);
			}
			this.FIS.close();
			this.FOS.close();

			return true;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				this.FIS.close();
				this.FOS.close();
			} catch (IOException f) {
				f.printStackTrace();
			}
		}
	}
}