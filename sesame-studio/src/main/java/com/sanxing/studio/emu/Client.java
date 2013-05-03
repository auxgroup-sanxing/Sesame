package com.sanxing.studio.emu;

import java.io.IOException;
import org.jdom.Element;

public abstract interface Client extends Runnable {
	public abstract Element send(String paramString, Element paramElement,
			int paramInt) throws IOException;
}