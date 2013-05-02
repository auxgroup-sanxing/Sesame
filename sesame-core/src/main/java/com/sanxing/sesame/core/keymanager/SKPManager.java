package com.sanxing.sesame.core.keymanager;

import com.sanxing.sesame.pwd.PasswordTool;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomDriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SKPManager {
	private static SKPManager _instance;
	private Map<String, ServiceKeyProvider> skps = new HashMap();

	public static synchronized SKPManager getInstance() {
		if (_instance == null) {
			_instance = new SKPManager();
		}
		return _instance;
	}

	public void addSKP(ServiceKeyProvider skp) throws Exception {
		skp.setKeyPass(PasswordTool.encrypt(skp.getKeyPass()));
		this.skps.put(skp.getName(), skp);
		persistence();
	}

	public ServiceKeyProvider getSKP(String name) throws Exception {
		ServiceKeyProvider skp = (ServiceKeyProvider) this.skps.get(name);
		ServiceKeyProvider temp = new ServiceKeyProvider();
		temp.setName(skp.getName());
		temp.setAlias(skp.getAlias());
		temp.setKeystoreName(skp.getKeystoreName());
		temp.setKeyPass(PasswordTool.decrypt(skp.getKeyPass()));
		temp.setPri(skp.isPri());

		return temp;
	}

	public List<ServiceKeyProvider> getAllKeyProvider() {
		List list = new ArrayList();
		try {
			Iterator itr = this.skps.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				ServiceKeyProvider skp = getSKP((String) entry.getKey());
				list.add(skp);
			}
		} catch (Exception e) {
			return null;
		}
		return list;
	}

	public void delSKP(String name) throws Exception {
		this.skps.remove(name);
		persistence();
	}

	private void persistence() throws FileNotFoundException {
		String serverDir = System.getProperty("SESAME_HOME");
		OutputStream out = new FileOutputStream(serverDir + File.separator
				+ "security" + File.separator + "skps.config");
		XStream xstream = new XStream(new JDomDriver());
		xstream.toXML(this.skps, out);
	}

	private SKPManager() {
		String serverDir = System.getProperty("SESAME_HOME");
		XStream xstream = new XStream(new JDomDriver());
		File secFolder = new File(serverDir + File.separator + "security");
		if (!(secFolder.exists())) {
			secFolder.mkdirs();
		}
		File skpsFile = new File(secFolder, "skps.config");

		if (skpsFile.exists())
			try {
				this.skps = ((Map) xstream
						.fromXML(new FileInputStream(skpsFile)));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		else
			try {
				this.skps = new HashMap();
				skpsFile.createNewFile();
				FileOutputStream fout = new FileOutputStream(skpsFile);
				xstream.toXML(this.skps, fout);
				fout.flush();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
	}
}