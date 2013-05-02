package com.sanxing.adp.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class ADPBaseProject {
	private String projectPath;
	private static Logger LOG = LoggerFactory.getLogger(ADPBaseProject.class);

	public ADPBaseProject(String projectPath) {
		this.projectPath = projectPath;
	}

	public void addClassPath(String jarName) throws Exception {
		try {
			deleteClassPath(jarName);
			String classPathFileName = this.projectPath + ".classpath";
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(classPathFileName));
			Element classpathEntry = new Element("classpathentry");
			classpathEntry.setAttribute("exported", "true");
			classpathEntry.setAttribute("kind", "lib");
			classpathEntry.setAttribute("path", "lib/" + jarName);
			doc.getRootElement().addContent(classpathEntry);
			persistence(classPathFileName, doc);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw e;
		}
	}

	public void deleteClassPath(String jarName) throws Exception {
		try {
			String filePath = "lib/" + jarName;
			String classPathFileName = this.projectPath + ".classpath";
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(classPathFileName));
			Element root = doc.getRootElement();

			List list = root.getChildren("classpathentry");
			for (int i = 0; i < list.size(); ++i) {
				Element entry = (Element) list.get(i);
				if (filePath.equals(entry.getAttributeValue("path"))) {
					root.removeContent(entry);
				}
			}
			persistence(classPathFileName, doc);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw e;
		}
	}

	private void persistence(String classPathFileName, Document doc)
			throws Exception {
		FileOutputStream fout = null;
		try {
			XMLOutputter output = new XMLOutputter();
			fout = new FileOutputStream(new File(classPathFileName));
			output.setFormat(Format.getPrettyFormat());
			output.output(doc, fout);
			fout.flush();
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw e;
		} finally {
			if (fout != null)
				try {
					fout.close();
				} catch (Exception e) {
					LOG.trace(e.getMessage(), e);
				}
		}
	}
}