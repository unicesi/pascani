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
 * Authors:	Gabriel Tamura 
 * 			Christophe Demarey
 */
package org.ow2.scesame.qoscare.core.scaspec;

import java.util.ArrayList;
import java.util.Collection;

public class SCAPort extends SCANamedNode {

	public String wiredTo;
	public SCAInterface implement;
	public Collection<SCABinding> bindings = new ArrayList<SCABinding>();

	/**
	 * Default constructor
	 * 
	 * @param name
	 *            The Port name.
	 */
	public SCAPort(String name) {
		super(name);
		implement = null;
		this.wiredTo = "Unwired";
	}

	public SCAPort(String name, String wiredTo) {
		super(name);
		implement = null;
		this.wiredTo = wiredTo;
	}

	public SCAPort(String name, String wiredTo, SCAInterface implement,
			Collection<SCABinding> bindings) {
		this(name, wiredTo);
		this.implement = implement;
		this.bindings.addAll(bindings);
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder("Interface ");
		sb.append(name).append("\n");
		sb.append("  wiredTo=").append(wiredTo).append("\n");
		sb.append("  bindings:\n");
		for (SCABinding b : this.bindings) {
			sb.append("  kind=").append(b.type).append("\n");
			for (SCAAttribute att : b.attributs) {
				sb.append(att.name).append("=").append(att.value).append("\n");
			}
		}

		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public String getWiredTo() {
		return wiredTo;
	}

	public Collection<SCABinding> getBindings() {
		return bindings;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setWiredTo(String wiredTo) {
		this.wiredTo = wiredTo;
	}

	public void setBindings(Collection<SCABinding> bindings) {
		this.bindings = bindings;
	}
}
