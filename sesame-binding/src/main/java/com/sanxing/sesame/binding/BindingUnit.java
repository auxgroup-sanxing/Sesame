package com.sanxing.sesame.binding;

import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.service.ServiceUnit;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Element;

public class BindingUnit {
	private static Logger LOG = LoggerFactory.getLogger(BindingUnit.class);
	private static Map<ServiceUnit, BindingUnit> units = new Hashtable();
	private ServiceUnit unit;
	private Transport transport;
	private Map<String, Map> messageTable = new Hashtable();

	public static BindingUnit newInstance(ServiceUnit serviceUnit)
			throws IOException {
		BindingUnit bindingUnit = (BindingUnit) units.get(serviceUnit);
		if (bindingUnit != null) {
			return bindingUnit;
		}
		bindingUnit = new BindingUnit(serviceUnit);
		units.put(serviceUnit, bindingUnit);
		return bindingUnit;
	}

	private BindingUnit(ServiceUnit unit) throws IOException {
		this.unit = unit;
	}

	public Definition getDefinition() {
		return this.unit.getDefinition();
	}

	public QName getServiceName() {
		return this.unit.getServiceName();
	}

	public Service getService() {
		return this.unit.getService();
	}

	public String getServiceUnitName() {
		return this.unit.getDefinition().getQName().getLocalPart();
	}

	public String getLocation() {
		Port port = null;
		Service service = this.unit.getService();
		Collection ports = service.getPorts().values();
		if (ports.isEmpty()) {
			return null;
		}
		port = (Port) ports.iterator().next();
		Iterator iter = port.getExtensibilityElements().iterator();
		if (!(iter.hasNext()))
			return null;

		ExtensibilityElement extEl = (ExtensibilityElement) iter.next();
		String location;
		if (extEl instanceof SOAPAddress) {
			SOAPAddress el = (SOAPAddress) extEl;
			location = el.getLocationURI();
		} else {
			UnknownExtensibilityElement el = (UnknownExtensibilityElement) extEl;
			location =  el.getElement().getAttribute("location");
		}
		return location;
	}

	public XmlSchema getSchema(String operationName) {
		return this.unit.getSchema(operationName);
	}

	public XmlSchemaCollection getSchemaCollection() {
		return this.unit.getSchemaCollection();
	}

	public ServiceUnit getServiceUnit() {
		return this.unit;
	}

	public Transport getTransport() {
		return this.transport;
	}

	public File getUnitRoot() {
		return this.unit.getUnitRoot();
	}

	public void destroy() {
		this.messageTable.clear();

		for (Map.Entry entry : units.entrySet())
			if (((BindingUnit) entry.getValue()).equals(this)) {
				units.remove(entry.getKey());
				return;
			}
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}
}