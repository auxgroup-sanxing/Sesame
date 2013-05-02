package com.sanxing.sesame.binding;

import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FaultHandler;
import java.util.Map;

public class Codec {
	private Decoder decoder;
	private Encoder encoder;
	private FaultHandler faultHandler;
	private Map<String, String> properties;

	public Decoder getDecoder() {
		return this.decoder;
	}

	public void setDecoder(Decoder decoder) {
		this.decoder = decoder;
	}

	public Encoder getEncoder() {
		return this.encoder;
	}

	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	public FaultHandler getFaultHandler() {
		return this.faultHandler;
	}

	public void setFaultHandler(FaultHandler faultHandler) {
		this.faultHandler = faultHandler;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}