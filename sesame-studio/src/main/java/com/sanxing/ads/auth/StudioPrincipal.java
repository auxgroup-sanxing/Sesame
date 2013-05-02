package com.sanxing.ads.auth;

import java.security.Principal;

public class StudioPrincipal implements Principal {
	private final String name;
	private String passwd;
	private String fullname;
	private String level;
	private String description;
	private transient int hash;

	public StudioPrincipal(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name cannot be null");
		}
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (super.getClass() != o.getClass())) {
			return false;
		}

		StudioPrincipal that = (StudioPrincipal) o;

		return (this.name.equals(that.name));
	}

	public int hashCode() {
		if (this.hash == 0) {
			this.hash = this.name.hashCode();
		}
		return this.hash;
	}

	public String toString() {
		return this.name;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getPasswd() {
		return this.passwd;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getFullname() {
		return this.fullname;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLevel() {
		return this.level;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
}