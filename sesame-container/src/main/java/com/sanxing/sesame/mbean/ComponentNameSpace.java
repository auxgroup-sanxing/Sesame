package com.sanxing.sesame.mbean;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ComponentNameSpace implements Externalizable {
	private static final long serialVersionUID = -9130913368962887486L;
	protected String containerName;
	protected String name;

	public ComponentNameSpace() {
	}

	public ComponentNameSpace(String containerName, String componentName) {
		this.containerName = containerName;
		this.name = componentName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String componentName) {
		this.name = componentName;
	}

	public String getContainerName() {
		return this.containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof ComponentNameSpace) {
			ComponentNameSpace other = (ComponentNameSpace) obj;
			result = (other.containerName.equals(this.containerName))
					&& (other.name.equals(this.name));
		}

		return result;
	}

	public int hashCode() {
		return (this.containerName.hashCode() ^ this.name.hashCode());
	}

	public String toString() {
		return "[container=" + this.containerName + ",name=" + this.name + "]";
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF((this.containerName != null) ? this.containerName : "");
		out.writeUTF((this.name != null) ? this.name : "");
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.containerName = in.readUTF();
		this.name = in.readUTF();
	}

	public ComponentNameSpace copy() {
		return new ComponentNameSpace(this.containerName, this.name);
	}
}