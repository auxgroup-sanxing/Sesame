package com.sanxing.ads.auth;

import javax.security.auth.callback.Callback;

public class PassiveCallback implements Callback {
	private String user;
	private String password;

	public String getUser() {
		return this.user;
	}

	public String getPassword() {
		return this.password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}