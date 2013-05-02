package com.sanxing.sesame.mbean;

import com.sanxing.sesame.component.ClientComponent;
import com.sanxing.sesame.exception.FaultException;
import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.jaxp.StringSource;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import com.sanxing.sesame.servicedesc.ExternalEndpoint;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import com.sanxing.sesame.servicedesc.LinkedEndpoint;
import com.sanxing.sesame.util.QNameUtil;
import com.sanxing.sesame.util.W3CUtil;
import java.beans.PropertyChangeListener;
import java.net.URI;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Endpoint implements EndpointMBean, MBeanInfoProvider {
	private static final Log LOG = LogFactory.getLog(Endpoint.class);
	private AbstractEndpoint endpoint;
	private Registry registry;

	public Endpoint(AbstractEndpoint endpoint, Registry registry) {
		this.endpoint = endpoint;
		this.registry = registry;
	}

	public String getRemoteContainers() {
		String temp = "";
		InternalEndpoint ipoint = (InternalEndpoint) this.endpoint;
		for (InternalEndpoint rpoint : ipoint.getRemoteEndpoints()) {
			temp = temp + rpoint.getComponentNameSpace().getContainerName();
		}
		return temp;
	}

	public String getEndpointName() {
		return this.endpoint.getEndpointName();
	}

	public QName[] getInterfaces() {
		return this.endpoint.getInterfaces();
	}

	public QName getServiceName() {
		return this.endpoint.getServiceName();
	}

	public String loadReference() {
		try {
			return W3CUtil.asIndentedXML(this.endpoint.getAsReference(null));
		} catch (TransformerException e) {
		}
		return null;
	}

	public String loadWSDL() {
		try {
			return W3CUtil.asXML(this.registry
					.getEndpointDescriptor(this.endpoint));
		} catch (Exception e) {
		}
		return null;
	}

	public String getComponentName() {
		if (this.endpoint.getComponentNameSpace() != null) {
			return this.endpoint.getComponentNameSpace().getName();
		}
		return null;
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "endpointName",
				"name of the endpoint");
		helper.addAttribute(getObjectToManage(), "serviceName",
				"name of the service");
		helper.addAttribute(getObjectToManage(), "componentName",
				"component name of the service unit");
		helper.addAttribute(getObjectToManage(), "interfaces",
				"interfaces implemented by this endpoint");
		helper.addAttribute(getObjectToManage(), "remoteContainers",
				"containers of remote endpoints");
		return helper.getAttributeInfos();
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		helper.addOperation(getObjectToManage(), "loadReference",
				"retrieve the endpoint reference");
		helper.addOperation(getObjectToManage(), "loadWSDL",
				"retrieve the wsdl description of this endpoint");
		helper.addOperation(getObjectToManage(), "send",
				"send a simple message exchange to test this endpoint");
		return helper.getOperationInfos();
	}

	public Object getObjectToManage() {
		return this;
	}

	public String getName() {
		return this.endpoint.getServiceName() + this.endpoint.getEndpointName();
	}

	public String getType() {
		return "Endpoint";
	}

	public String getSubType() {
		if (this.endpoint instanceof InternalEndpoint)
			return "Internal";
		if (this.endpoint instanceof LinkedEndpoint)
			return "Linked";
		if (this.endpoint instanceof ExternalEndpoint) {
			return "External";
		}
		return null;
	}

	public String getDescription() {
		return null;
	}

	public void setPropertyChangeListener(PropertyChangeListener l) {
	}

	protected AbstractEndpoint getEndpoint() {
		return this.endpoint;
	}

	public String send(String content, String operation, String mep) {
		try {
			ClientComponent client = ClientComponent.getInstance();
			MessageExchange me = client.getExchangeFactory().createExchange(
					URI.create(mep));
			NormalizedMessage nm = me.createMessage();
			me.setMessage(nm, "in");
			nm.setContent(new StringSource(content));
			me.setEndpoint(this.endpoint);
			if (operation != null) {
				me.setOperation(QNameUtil.parse(operation));
			}
			client.sendSync(me);
			if (me.getError() != null)
				throw me.getError();
			if (me.getFault() != null)
				throw FaultException.newInstance(me);
			if (me.getMessage("out") != null) {
				return new SourceTransformer().contentToString(me.getMessage("out"));
			}

			return null;
		} catch (Exception e) {
			return null;
		}
	}
}