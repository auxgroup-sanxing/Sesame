package com.sanxing.studio.utils;

import com.sanxing.studio.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import javax.wsdl.xml.WSDLLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class WSDLLocatorImpl implements WSDLLocator {
	private static Logger LOG = LoggerFactory.getLogger(WSDLLocatorImpl.class);
	private URI baseURI;
	private URI latestImportURI;
	private boolean ignore;
	private Map<URI, InputStream> openedResources = new Hashtable();

	public WSDLLocatorImpl(File file, boolean ignore) {
		this.baseURI = file.toURI();
		this.ignore = ignore;
	}

	public void close() {
		for (Map.Entry entry : this.openedResources.entrySet())
			try {
				InputStream input = (InputStream) entry.getValue();
				input.close();
			} catch (IOException e) {
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
						&& (importFile.getName().endsWith(".xsd"))) {
					this.latestImportURI = parentURI.resolve(importLocation);
					if ("file".equals(this.latestImportURI.getScheme())) {
						File file = new File(this.latestImportURI);
						if ((!(file.exists())) && (this.ignore)) {
							file = new File(
									Configuration
											.getRealPath("cache/sesame-platform.xsd"));
							this.latestImportURI = file.toURI();
							LOG.debug("latestImportURI: "
									+ this.latestImportURI);
						}
					}
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