package com.sanxing.sesame.binding.transport;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;
import java.io.IOException;

public abstract interface Acceptor extends Transport {
	public abstract void reply(MessageContext paramMessageContext)
			throws BindingException, IOException;
}