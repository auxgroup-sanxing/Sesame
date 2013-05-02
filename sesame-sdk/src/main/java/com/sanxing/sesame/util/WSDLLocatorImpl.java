package com.sanxing.sesame.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import javax.wsdl.xml.WSDLLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class WSDLLocatorImpl implements WSDLLocator {
	private static Logger LOG = LoggerFactory.getLogger(WSDLLocatorImpl.class);
	private URI baseURI;
	private URI latestImportURI;
	private Map<URI, InputStream> openedResources = new Hashtable();

	public WSDLLocatorImpl(File file) {
		this.baseURI = file.toURI();
	}

	public WSDLLocatorImpl(String documentBaseURI) throws URISyntaxException {
		this.baseURI = new URI(documentBaseURI);
	}

	public void close() {
		for (Map.Entry entry : this.openedResources.entrySet())
			try {
				InputStream input = (InputStream) entry.getValue();
				input.close();
			} catch (IOException localIOException) {
			}
	}

	public InputSource getBaseInputSource() {
		try {
			InputStream input = this.baseURI.toURL().openStream();
			this.openedResources.put(this.baseURI, input);
			InputSource inputSource = new InputSource(input);

			return inputSource;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public String getBaseURI() {
		return this.baseURI.toString();
	}

	public InputSource getImportInputSource(String parentLocation,
			String importLocation) {
		try {
			URI importURI = new URI(importLocation);
			URI parentURI = new URI(parentLocation);
			if ((importURI.getScheme() == null)
					&& ("file".equals(parentURI.getScheme()))) {
				File parentFile = new File(parentURI);
				File importFile = new File(importLocation);
				if ((parentFile.getName().equals("unit.wsdl"))
						&& (importFile.getName().equals("unit.wsdl"))) {
					String unitName = importFile.getParentFile().getName();

					File sus = parentFile.getParentFile().getParentFile()
							.getParentFile();
					boolean found = false;
					File[] list = sus.listFiles();
					for (File item : list) {
						if (!(item.isDirectory()))
							continue;
						for (File entry : item.listFiles()) {
							if (entry.getName().equals(unitName)) {
								this.latestImportURI = new File(entry,
										"unit.wsdl").toURI();
								LOG.debug("Import wsdl - "
										+ this.latestImportURI);
								found = true;
								break;
							}
						}
					}
					if (!(found))
						this.latestImportURI = parentURI
								.resolve(importLocation);
				} else {
					this.latestImportURI = parentURI.resolve(importLocation);
				}
			} else {
				this.latestImportURI = parentURI.resolve(importLocation);
			}
			InputStream input = (InputStream) this.openedResources
					.get(this.latestImportURI);
			if (input == null) {
				input = this.latestImportURI.toURL().openStream();
				this.openedResources.put(this.latestImportURI, input);
			}
			InputSource inputSource = new InputSource(input);
			return inputSource;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public String getLatestImportURI() {
		return this.latestImportURI.toString();
	}
}