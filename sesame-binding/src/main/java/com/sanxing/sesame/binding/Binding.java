package com.sanxing.sesame.binding;

import java.net.URI;
import java.util.Map;

import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.transform.Source;

import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.service.ServiceUnit;

public interface Binding
{
    public abstract void init( Codec codec, ServiceUnit serviceUnit, Service service, Port port );

    public abstract URI bind()
        throws BindingException;

    public abstract void unbind()
        throws BindingException;

    public abstract URI getAddress();

    public abstract OperationContext getOperationContext( String action );

    public abstract Transport getTransport();

    public abstract ServiceUnit getServiceUnit();

    public abstract boolean parse( MessageContext message, Map<String, Object> params )
        throws BindingException;

    public abstract boolean assemble( Source content, MessageContext message )
        throws BindingException;

    public abstract boolean handle( Source fault, MessageContext context )
        throws BindingException;
}