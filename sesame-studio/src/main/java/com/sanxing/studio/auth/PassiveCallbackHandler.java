package com.sanxing.studio.auth;

import java.io.IOException;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

public class PassiveCallbackHandler implements CallbackHandler {
	private String username;
	private String password;
	private Map<String, ?> params;

	public PassiveCallbackHandler(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public PassiveCallbackHandler(String username, Map<String, String> params) {
		this.username = username;
		this.params = params;
	}

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback : callbacks)
			if (callback instanceof PassiveCallback) {
				PassiveCallback passiveCallback = (PassiveCallback) callback;
				passiveCallback.setUser(this.username);
				passiveCallback.setPassword(this.password);
			} else if (callback instanceof DigestCallback) {
				DigestCallback digestCallback = (DigestCallback) callback;
				digestCallback.setUser(this.username);
				digestCallback.setParams(this.params);
			}
	}
}