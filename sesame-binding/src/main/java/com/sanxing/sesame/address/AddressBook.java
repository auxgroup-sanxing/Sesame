package com.sanxing.sesame.address;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AddressBook {
	private static final Logger LOG = LoggerFactory.getLogger(AddressBook.class);

	private static Map<String, Location> registry = new Hashtable();

	public static void init() {
		try {
			File file = new File(System.getProperty("SESAME_HOME"),
					"conf/address-book.xml");
			LOG.debug("Loading addresses from file: " + file);
			DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
			Document doc = bf.newDocumentBuilder().parse(file);
			Element root = doc.getDocumentElement();
			NodeList locations = root.getElementsByTagName("location");
			for (int i = 0; i < locations.getLength(); ++i) {
				Element locationEl = (Element) locations.item(i);
				String name = locationEl.getAttribute("name");
				URI uri = new URI(locationEl.getAttribute("url"));
				Location location = new Location(uri, locationEl,
						locationEl.getAttribute("style"));
				add(name, location);
			}
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
		}
	}

	public static void add(String name, Location location) {
		registry.put(name, location);
	}

	public static Location find(String name) {
		return ((Location) registry.get(name));
	}

	public static Location[] list() {
		Collection locations = registry.values();
		return ((Location[]) locations.toArray(new Location[locations.size()]));
	}

	public static void reload(String name) {
		registry.remove(name);
	}

	public static void remove(String name) {
		registry.remove(name);
	}

	public void clear() {
		registry.clear();
	}
}