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

public class SCABinding extends SCANamedNode {
	
	public static enum Kind {
		WS, REST, RMI, JSONRPC, JMS, UNKNOWN
	};

	public Collection<SCAAttribute> attributes = new ArrayList<SCAAttribute>();
	public Kind type;

	public SCABinding(Kind type) {
		super("binding");
		this.type = type;
	}

	public SCABinding(Kind type, Collection<SCAAttribute> attributes) {
		super("binding");
		this.type = type;
		this.attributes = attributes;
	}

	public String getKind() {
		return type.name();
	}

	public Collection<SCAAttribute> getAttributes() {
		return attributes;
	}

	public void setKind(String kind) {
		if (kind.compareTo("WS") == 0)
			this.type = Kind.WS;
		else if (kind.compareTo("REST") == 0)
			this.type = Kind.REST;
		else if (kind.compareTo("RMI") == 0)
			this.type = Kind.RMI;
		else if (kind.compareTo("JSONRPC") == 0)
			this.type = Kind.JSONRPC;
		else if (kind.compareTo("JMS") == 0)
			this.type = Kind.JMS;
	}

	public void setAttribute(Collection<SCAAttribute> attributes) {
		this.attributes = attributes;
	}

}
