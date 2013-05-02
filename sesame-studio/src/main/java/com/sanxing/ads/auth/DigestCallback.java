package com.sanxing.ads.auth;

import java.util.Map;
import javax.security.auth.callback.Callback;

public class DigestCallback implements Callback {
	private String user;
	private Map<String, ?> params;

	public String getUser() {
		return this.user;
	}

	public Map<String, ?> getParams() {
		return this.params;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setParams(Map<String, ?> params) {
		this.params = params;
	}
}