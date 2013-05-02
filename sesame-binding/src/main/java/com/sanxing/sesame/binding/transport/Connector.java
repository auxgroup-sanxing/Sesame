package com.sanxing.sesame.binding.transport;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;
import java.io.IOException;

public abstract interface Connector extends Transport {
	public abstract void sendOut(MessageContext paramMessageContext)
			throws BindingException, IOException;
}