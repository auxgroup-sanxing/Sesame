package com.sanxing.sesame.binding;

import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.service.ServiceUnit;
import java.net.URI;
import java.util.Map;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.transform.Source;

public abstract interface Binding {
	public abstract void init(Codec paramCodec, ServiceUnit paramServiceUnit,
			Service paramService, Port paramPort);

	public abstract URI bind() throws BindingException;

	public abstract void unbind() throws BindingException;

	public abstract URI getAddress();

	public abstract OperationContext getOperationContext(String paramString);

	public abstract Transport getTransport();

	public abstract ServiceUnit getServiceUnit();

	public abstract boolean parse(MessageContext paramMessageContext,
			Map<String, Object> paramMap) throws BindingException;

	public abstract boolean assemble(Source paramSource,
			MessageContext paramMessageContext) throws BindingException;

	public abstract boolean handle(Source paramSource,
			MessageContext paramMessageContext) throws BindingException;
}