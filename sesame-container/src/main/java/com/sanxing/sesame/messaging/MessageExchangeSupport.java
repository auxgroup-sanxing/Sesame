package com.sanxing.sesame.messaging;

import java.net.URI;

public final class MessageExchangeSupport {
	public static final URI IN_ONLY = URI
			.create("http://www.w3.org/2004/08/wsdl/in-only");

	public static final URI IN_OUT = URI
			.create("http://www.w3.org/2004/08/wsdl/in-out");

	public static final URI IN_OPTIONAL_OUT = URI
			.create("http://www.w3.org/2004/08/wsdl/in-opt-out");

	public static final URI OUT_ONLY = URI
			.create("http://www.w3.org/2004/08/wsdl/out-only");

	public static final URI OUT_IN = URI
			.create("http://www.w3.org/2004/08/wsdl/out-in");

	public static final URI OUT_OPTIONAL_IN = URI
			.create("http://www.w3.org/2004/08/wsdl/out-opt-in");

	public static final URI ROBUST_IN_ONLY = URI
			.create("http://www.w3.org/2004/08/wsdl/robust-in-only");

	public static final URI ROBUST_OUT_ONLY = URI
			.create("http://www.w3.org/2004/08/wsdl/robust-out-only");

	public static final URI WSDL2_IN_ONLY = URI
			.create("http://www.w3.org/2006/01/wsdl/in-only");

	public static final URI WSDL2_IN_OPTIONAL_OUT = URI
			.create("http://www.w3.org/2006/01/wsdl/in-opt-out");

	public static final URI WSDL2_IN_OUT = URI
			.create("http://www.w3.org/2006/01/wsdl/in-out");

	public static final URI WSDL2_ROBUST_IN_ONLY = URI
			.create("http://www.w3.org/2006/01/wsdl/robust-in-only");

	public static final URI WSDL2_OUT_ONLY = URI
			.create("http://www.w3.org/2006/01/wsdl/out-only");

	public static final URI WSDL2_OUT_IN = URI
			.create("http://www.w3.org/2006/01/wsdl/out-in");

	public static final URI WSDL2_OUT_OPTIONAL_IN = URI
			.create("http://www.w3.org/2006/01/wsdl/out-opt-in");

	public static final URI WSDL2_ROBUST_OUT_ONLY = URI
			.create("http://www.w3.org/2006/01/wsdl/robust-out-only");
}