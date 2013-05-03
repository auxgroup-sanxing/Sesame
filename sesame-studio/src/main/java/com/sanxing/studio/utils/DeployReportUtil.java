package com.sanxing.studio.utils;

import com.sanxing.sesame.core.keymanager.KeyStoreInfo;
import com.sanxing.sesame.core.keymanager.ServiceKeyProvider;
import com.lowagie.text.Chapter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomDriver;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class DeployReportUtil {
	private static final Logger LOG = LoggerFactory.getLogger(DeployReportUtil.class);

	private PdfWriter writer = null;

	private com.lowagie.text.Document document = new com.lowagie.text.Document();
	private String outputPath;
	private Font sectionFont;
	private Font chapterFont;
	private Font tableFont;
	private Font textFont;

	public DeployReportUtil(String outputPath) throws DocumentException,
			IOException {
		this.outputPath = outputPath;
		this.writer = PdfWriter.getInstance(this.document,
				new FileOutputStream(outputPath));

		this.document.open();
		initFont();
	}

	public void buildReport() {
		try {
			Chapter chapter = newChapter("环境参数配置");
			addAddressBookSection(chapter);
			addKeyStoreSection(chapter);
			addKeyProviderSection(chapter);
			addDatabaseSection(chapter);
			addJMSSection(chapter);
			addChapter(chapter);
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initFont() throws DocumentException, IOException {
		BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
				"UniGB-UCS2-H", false);

		this.sectionFont = new Font(bfChinese, 16.0F, 1, Color.black);
		this.chapterFont = new Font(bfChinese, 18.0F, 3, Color.BLUE);
		this.tableFont = new Font(bfChinese, 12.0F, 0, Color.black);
		this.textFont = new Font(bfChinese, 12.0F, 0, Color.black);
	}

	private Chapter newChapter(String title) {
		Paragraph pg = new Paragraph(title, this.chapterFont);
		Chapter chapter = new Chapter(pg, 1);
		chapter.setNumberDepth(0);
		return chapter;
	}

	private void addChapter(Chapter chapter) throws DocumentException {
		this.document.add(chapter);
	}

	private void close() {
		this.document.close();
		this.writer.close();
	}

	private void addAddressBookSection(Chapter chapter) throws JDOMException,
			IOException {
		Paragraph title11 = new Paragraph("地址簿", this.sectionFont);
		Section section1 = chapter.addSection(title11);
		Paragraph someSectionText = new Paragraph("以下是地址簿列表，请按照需要配置.",
				this.textFont);
		section1.add(someSectionText);
		someSectionText = new Paragraph("  ", this.textFont);
		section1.add(someSectionText);

		File file = new File(System.getProperty("SESAME_HOME"),
				"conf/address-book.xml");

		if (!(file.exists())) {
			return;
		}
		LOG.debug("Loading addresses from file: " + file);
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document doc = builder.build(file);
		Element root = doc.getRootElement();
		Iterator itr = root.getChildren("location").iterator();

		PdfPTable table = new PdfPTable(2);
		PdfPCell cell = null;
		while (itr.hasNext()) {
			Element locationEl = (Element) itr.next();
			cell = new PdfPCell(new Paragraph("name", this.tableFont));
			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(
					locationEl.getAttributeValue("name"), this.tableFont));

			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("url", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(
					locationEl.getAttributeValue("url"), this.tableFont));

			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("style", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(
					locationEl.getAttributeValue("style"), this.tableFont));

			table.addCell(cell);
		}

		section1.add(table);
	}

	private void addKeyStoreSection(Chapter chapter)
			throws FileNotFoundException {
		Paragraph title11 = new Paragraph("keystore", this.sectionFont);
		Section section1 = chapter.addSection(title11);
		Paragraph someSectionText = new Paragraph("以下是keystore列表，请按照需要配置.",
				this.textFont);

		section1.add(someSectionText);
		someSectionText = new Paragraph("  ", this.textFont);
		section1.add(someSectionText);

		Map stores = null;

		File storeFiles = new File(System.getProperty("SESAME_HOME"),
				"security/keystore.config");

		if (!(storeFiles.exists())) {
			return;
		}
		XStream xstream = new XStream(new JDomDriver());
		stores = (Map) xstream.fromXML(new FileInputStream(storeFiles));

		Iterator itr = stores.entrySet().iterator();
		PdfPTable table = new PdfPTable(2);
		PdfPCell cell = null;
		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();

			KeyStoreInfo info = (KeyStoreInfo) entry.getValue();

			cell = new PdfPCell(new Paragraph("keystore名称", this.tableFont));
			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(info.getName(), this.tableFont));
			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("keystore路径", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(info.getKeystorePath(),
					this.tableFont));

			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("描述", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(info.getDescription(),
					this.tableFont));
			table.addCell(cell);
		}

		section1.add(table);
	}

	private void addKeyProviderSection(Chapter chapter)
			throws FileNotFoundException {
		Paragraph title11 = new Paragraph("keyprovier", this.sectionFont);
		Section section1 = chapter.addSection(title11);
		Paragraph someSectionText = new Paragraph("以下是keyprovier列表，请按照需要配置.",
				this.textFont);

		section1.add(someSectionText);
		someSectionText = new Paragraph("  ", this.textFont);
		section1.add(someSectionText);

		Map stores = null;

		File storeFiles = new File(System.getProperty("SESAME_HOME"),
				"security/skps.config");

		if (!(storeFiles.exists())) {
			return;
		}
		XStream xstream = new XStream(new JDomDriver());
		stores = (Map) xstream.fromXML(new FileInputStream(storeFiles));

		Iterator itr = stores.entrySet().iterator();
		PdfPTable table = new PdfPTable(2);
		PdfPCell cell = null;
		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();

			ServiceKeyProvider info = (ServiceKeyProvider) entry.getValue();

			cell = new PdfPCell(new Paragraph("keyprovider名称", this.tableFont));
			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(info.getName(), this.tableFont));
			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("别名", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(info.getAlias(), this.tableFont));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("keystore名称 ", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(info.getKeystoreName(),
					this.tableFont));

			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("密钥是否成对  ", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph((info.isPri()) ? "是" : "否",
					this.tableFont));
			table.addCell(cell);
		}

		section1.add(table);
	}

	private void addJMSSection(Chapter chapter) throws JDOMException,
			IOException {
		Paragraph title11 = new Paragraph("消息队列", this.sectionFont);
		Section section1 = chapter.addSection(title11);
		Paragraph someSectionText = new Paragraph("以下是消息队列列表，请按照需要配置.",
				this.textFont);

		section1.add(someSectionText);
		someSectionText = new Paragraph("  ", this.textFont);
		section1.add(someSectionText);

		String serverName = System.getProperty("server-name");
		if (serverName == null) {
			return;
		}
		File file = new File(System.getProperty("SESAME_HOME"), "conf/"
				+ serverName + ".xml");

		if (!(file.exists())) {
			return;
		}
		LOG.debug("Loading addresses from file: " + file);
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document doc = builder.build(file);
		Element root = doc.getRootElement();
		Iterator itr = root.getChildren("jms").iterator();

		PdfPTable table = new PdfPTable(2);
		PdfPCell cell = null;
		while (itr.hasNext()) {
			Element jms = (Element) itr.next();
			Element appinfo = jms.getChild("app-info");
			if (appinfo == null) {
				continue;
			}

			cell = new PdfPCell(new Paragraph("JNDI名称", this.tableFont));
			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(
					appinfo.getChildText("jndi-name"), this.tableFont));

			cell.setBackgroundColor(new Color(192, 192, 192));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("消息类型 ", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(appinfo.getChildText("type"),
					this.tableFont));

			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("消息路由端口", this.tableFont));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(
					appinfo.getChildText("activemq-broker-port"),
					this.tableFont));

			table.addCell(cell);
		}

		section1.add(table);
	}

	private void addDatabaseSection(Chapter chapter) throws JDOMException,
			IOException {
		Paragraph title11 = new Paragraph("数据源", this.sectionFont);
		Section section1 = chapter.addSection(title11);
		Paragraph someSectionText = new Paragraph("以下是数据源列表，请按照需要配置.",
				this.textFont);
		section1.add(someSectionText);
		someSectionText = new Paragraph("  ", this.textFont);
		section1.add(someSectionText);

		String serverName = System.getProperty("server-name");
		if (serverName == null) {
			return;
		}
		File file = new File(System.getProperty("SESAME_HOME"), "conf/"
				+ serverName + ".xml");

		if (!(file.exists())) {
			return;
		}
		LOG.debug("Loading database from file: " + file);
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document doc = builder.build(file);
		Element root = doc.getRootElement();
		Iterator itr = root.getChildren("jdbc").iterator();

		PdfPTable table = new PdfPTable(2);
		PdfPCell cell = null;
		while (itr.hasNext()) {
			Element jdbc = (Element) itr.next();
			if (jdbc == null) {
				continue;
			}
			Iterator dbIter = jdbc.getChildren("datasource").iterator();
			while (dbIter.hasNext()) {
				Element database = (Element) dbIter.next();
				if (database == null) {
					continue;
				}
				Element jndi = database.getChild("jndi-name");
				Element appinfo = database.getChild("app-info");
				if (jndi == null)
					continue;
				if (appinfo == null) {
					continue;
				}

				cell = new PdfPCell(new Paragraph("JNDI名称", this.tableFont));
				cell.setBackgroundColor(new Color(192, 192, 192));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(jndi.getText(),
						this.tableFont));
				cell.setBackgroundColor(new Color(192, 192, 192));
				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("驱动类", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(
						appinfo.getChildText("driver-class"), this.tableFont));

				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("连接URL", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(appinfo.getChildText("url"),
						this.tableFont));

				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("用户名", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(
						appinfo.getChildText("username"), this.tableFont));

				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("密码", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(
						appinfo.getChildText("password"), this.tableFont));

				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("最大空闲连接", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(
						appinfo.getChildText("max-idle"), this.tableFont));

				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("最大等待连接时间", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(
						appinfo.getChildText("max-wait"), this.tableFont));

				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("最大活动连接数", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(
						appinfo.getChildText("max-active"), this.tableFont));

				table.addCell(cell);

				cell = new PdfPCell(new Paragraph("初始化连接池数量", this.tableFont));
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(
						appinfo.getChildText("initial-size"), this.tableFont));

				table.addCell(cell);
			}

		}

		section1.add(table);
	}
}