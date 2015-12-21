/***
 * OW2 SCeSAME QoS-CARE Core 
 * Copyright (C) 2010-2011 G. Tamura
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Authors: Gabriel Tamura 
 * 			Christophe Demarey
 */
package org.ow2.scesame.qoscare.core.scaspec;

import java.util.ArrayList;
import java.util.Collection;

import org.ow2.scesame.qoscare.core.scaspec.SCANamedNode;

/**
 * This class is a REST resource used to represent an SCA component/composite.
 */
public class SCAComponent extends SCANamedNode {
	
	public static enum Status {
		STARTED, STOPPED, UNKNOWN
	};

	public Status status = Status.STOPPED;

	public Collection<SCAComponent> children = new ArrayList<SCAComponent>();
	public Collection<SCAProperty> properties = new ArrayList<SCAProperty>();
	public Collection<SCAPort> services = new ArrayList<SCAPort>();
	public Collection<SCAPort> references = new ArrayList<SCAPort>();

	/**
	 * Get a Component with a set name.
	 * 
	 * @param name
	 *            The component name.
	 */
	public SCAComponent(String name) {
		this(name, Status.STOPPED);
	}

	/**
	 * Get a Component with name and status set.
	 * 
	 * @param name
	 *            The component name.
	 * @param status
	 *            The component status.
	 */
	public SCAComponent(String name, Status status) {
		super(name);
		this.status = status;
	}

	/**
	 * @return true if this is a composite (children.isEmpty()).
	 */
	public boolean isComposite() {
		if (!this.children.isEmpty())
			return true;
		else
			return false;
	}

	/**
	 * Sets a Component as started.
	 */
	public void setStarted() {
		this.status = Status.STARTED;
	}

	/**
	 * Sets a Component as stopped.
	 */
	public void setStopped() {
		this.status = Status.STOPPED;
	}
	
	public String getName() {
		return name;
	}

	public Collection<SCAPort> getServices() {
		return this.services;
	}

	public Collection<SCAPort> getReferences() {
		return this.references;
	}

	public Collection<SCAProperty> getProperties() {
		return this.properties;
	}

	public Collection<SCAComponent> getChildren() {
		return this.children;
	}

	public void setServices(Collection<SCAPort> svcs) {
		this.services = svcs;
	}

	public void addService(SCAPort svc) {
		this.services.add(svc);
	}

	public void setReferences(Collection<SCAPort> refs) {
		this.references = refs;
	}

	public void addReference(SCAPort ref) {
		this.references.add(ref);
	}

	public void setProperties(Collection<SCAProperty> props) {
		this.properties = props;
	}

	public void addProperty(SCAProperty prop) {
		this.properties.add(prop);
	}

	public void setChildren(Collection<SCAComponent> children) {
		this.children = children;
	}

	public void addChild(SCAComponent child) {
		this.children.add(child);
	}

	@Override public String toString() {
		StringBuilder sb;
		if (this.isComposite())
			sb = new StringBuilder("Composite ");
		else
			sb = new StringBuilder("Component ");
		sb.append(this.name).append(" status=").append(this.status);

		if (this.isComposite()) {
			sb.append("\nchildren:\n");
			for (SCAComponent child : this.children) {
				sb.append("  ").append(child.name).append("\n");
			}
		}
		sb.append("services:\n");
		for (SCAPort p : this.services) {
			sb.append("  ").append(p.name).append("\n");
		}
		sb.append("references:\n");
		for (SCAPort p : this.references) {
			sb.append("  ").append(p.name).append("\n");
		}
		sb.append("properties:\n");
		for (SCAProperty p : this.properties) {
			sb.append("  name=").append(p.name).append(" value='")
					.append(p.value).append("' type=").append(p.type)
					.append("\n");
		}

		return sb.toString();
	}

}
