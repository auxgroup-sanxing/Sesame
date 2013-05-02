package com.sanxing.ads.utils;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class Transfer {
	public static void transform(Source xml, Source xslt, StreamResult result)
			throws Exception {
		TransformerFactory transFact = TransformerFactory.newInstance();
		Transformer trans = transFact.newTransformer(xslt);
		trans.transform(xml, result);
	}
}