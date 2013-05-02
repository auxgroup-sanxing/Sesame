package com.sanxing.sesame.mbean;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class ArchiveEntry implements Serializable {
	protected String location;
	protected Date lastModified;
	protected String type;
	protected String name;
	protected boolean pending;
	protected transient Set<String> dependencies;

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPending() {
		return this.pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public Set<String> getDependencies() {
		return this.dependencies;
	}

	public void setDependencies(Set<String> dependencies) {
		this.dependencies = dependencies;
	}

	public String toString() {
		return "ArchiveEntry [lastModified=" + this.lastModified
				+ ", location=" + this.location + ", name=" + this.name
				+ ", pending=" + this.pending + ", type=" + this.type + "]";
	}
}