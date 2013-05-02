package com.sanxing.sesame.binding.codec;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class CodecFactory {
	private static Map<String, Class<? extends Decoder>> decoderClasses = new Hashtable();

	private static Map<String, Class<? extends Encoder>> encoderClasses = new Hashtable();

	private static Map<String, Decoder> decoders = new Hashtable();

	private static Map<String, Encoder> encoders = new Hashtable();

	public static Decoder createDecoder(String codeName)
			throws InstantiationException {
		Class clazz = (Class) decoderClasses.get(codeName);
		if (clazz == null) {
			throw new NullPointerException("No implementation for '" + codeName
					+ "'");
		}
		try {
			return ((Decoder) clazz.newInstance());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Encoder createEncoder(String codeName)
			throws InstantiationException {
		Class clazz = (Class) encoderClasses.get(codeName);
		if (clazz == null) {
			throw new NullPointerException("No implementation for '" + codeName
					+ "'");
		}
		try {
			return ((Encoder) clazz.newInstance());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Set<String> getDecoderNames() {
		return Collections.unmodifiableSet(decoderClasses.keySet());
	}

	public static Set<String> getEncoderNames() {
		return Collections.unmodifiableSet(encoderClasses.keySet());
	}

	public static void register(String codeName, Class<?> clazz) {
		if (clazz.isInterface()) {
			throw new RuntimeException(clazz.getName()
					+ " must be an implementation");
		}

		if (Decoder.class.isAssignableFrom(clazz)) {
			decoderClasses.put(codeName, clazz.asSubclass(Decoder.class));
		}
		if (Encoder.class.isAssignableFrom(clazz))
			encoderClasses.put(codeName, clazz.asSubclass(Encoder.class));
	}

	public static void register(String codeName, Decoder decoder) {
		decoders.put(codeName, decoder);
	}

	public static void register(String codeName, Encoder encoder) {
		encoders.put(codeName, encoder);
	}

	public static void unregister(String codeName) {
		decoders.remove(codeName);
		encoders.remove(codeName);

		decoderClasses.remove(codeName);
		encoderClasses.remove(codeName);
	}
}