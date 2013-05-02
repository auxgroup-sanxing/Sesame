package com.sanxing.sesame.jmx.security;

import java.security.Principal;

public class GroupPrincipal implements Principal {
	public static final GroupPrincipal ANY = new GroupPrincipal("*");
	private final String name;
	private transient int hash;

	public GroupPrincipal(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
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
		GroupPrincipal that = (GroupPrincipal) o;

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
}